package ru.fintracker.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.fintracker.R
import ru.fintracker.core.Dates
import ru.fintracker.core.Money
import ru.fintracker.databinding.ItemDateHeaderBinding
import ru.fintracker.databinding.ItemTotalFooterBinding
import ru.fintracker.databinding.ItemTransactionBinding
import ru.fintracker.data.model.TransactionType
import ru.fintracker.ui.common.color
import ru.fintracker.ui.common.operationsCount
import ru.fintracker.ui.common.showIf

class TransactionAdapter(
    private val onClick: (TransactionListItem.Row) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(Diff) {

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is TransactionListItem.Header -> TYPE_HEADER
        is TransactionListItem.Row -> if (item.item.transaction.type == TransactionType.INCOME) {
            TYPE_INCOME
        } else {
            TYPE_EXPENSE
        }
        is TransactionListItem.Footer -> TYPE_FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(ItemDateHeaderBinding.inflate(inflater, parent, false))
            TYPE_FOOTER -> FooterViewHolder(ItemTotalFooterBinding.inflate(inflater, parent, false))
            else -> RowVH(
                ItemTransactionBinding.inflate(inflater, parent, false),
                income = viewType == TYPE_INCOME
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderVH).bind(item)
            is TransactionListItem.Row -> (holder as RowVH).bind(item)
            is TransactionListItem.Footer -> (holder as FooterViewHolder).bind(item)
        }
    }

    private class HeaderVH(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionListItem.Header) {
            binding.tvDate.text = Dates.dayHeader(item.date)
            binding.tvDayTotal.text = when {
                item.expenseMinor > 0L && item.incomeMinor > 0L ->
                    "− ${Money.format(item.expenseMinor)} · + ${Money.format(item.incomeMinor)}"
                item.expenseMinor > 0L -> "− ${Money.format(item.expenseMinor)}"
                item.incomeMinor > 0L -> "+ ${Money.format(item.incomeMinor)}"
                else -> ""
            }
        }
    }

    private inner class RowVH(
        private val binding: ItemTransactionBinding,
        private val income: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionListItem.Row) {
            val context = binding.root.context
            val transaction = item.item.transaction
            val category = item.category

            binding.tvEmoji.text = category?.emoji ?: "❓"
            val baseColor = category?.color ?: context.color(R.color.legend_other)
            binding.iconContainer.setCardBackgroundColor(
                ColorUtils.setAlphaComponent(baseColor, 40)
            )
            binding.tvTitle.text = category?.name ?: "Без категории"

            val tags = item.item.tags.joinToString(" ") { "#${it.name}" }
            val subtitle = listOfNotNull(
                transaction.note.takeIf { it.isNotBlank() },
                tags.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            binding.tvSubtitle.text = subtitle
            binding.tvSubtitle.showIf(subtitle.isNotEmpty())

            binding.tvAmount.text =
                "${if (income) "+" else "−"} ${Money.format(transaction.amountMinor)}"
            binding.tvAmount.setTextColor(
                context.color(if (income) R.color.income else R.color.expense)
            )
            binding.tvTime.text = Dates.time(transaction.date)
            binding.rowCard.setCardBackgroundColor(
                context.color(if (income) R.color.income_bg else R.color.expense_bg)
            )
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private class FooterViewHolder(
        private val binding: ItemTotalFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionListItem.Footer) {
            binding.tvFooterCount.text = operationsCount(item.count)
            binding.tvFooterExpense.text = "− ${Money.format(item.expenseMinor)}"
            binding.tvFooterIncome.text = "+ ${Money.format(item.incomeMinor)}"
        }
    }

    private object Diff : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(
            oldItem: TransactionListItem,
            newItem: TransactionListItem
        ): Boolean = when {
            oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header ->
                oldItem.date == newItem.date
            oldItem is TransactionListItem.Row && newItem is TransactionListItem.Row ->
                oldItem.item.transaction.id == newItem.item.transaction.id
            oldItem is TransactionListItem.Footer && newItem is TransactionListItem.Footer -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: TransactionListItem,
            newItem: TransactionListItem
        ): Boolean = oldItem == newItem
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_EXPENSE = 1
        const val TYPE_INCOME = 2
        const val TYPE_FOOTER = 3
    }
}
