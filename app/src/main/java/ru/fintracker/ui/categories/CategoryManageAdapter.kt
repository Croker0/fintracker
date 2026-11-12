package ru.fintracker.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.fintracker.core.Money
import ru.fintracker.databinding.ItemCategoryManageBinding
import ru.fintracker.ui.common.operationsCount

data class CategoryUi(
    val category: ru.fintracker.data.local.entity.CategoryEntity,
    val usageCount: Long,
    val totalMinor: Long
)

class CategoryManageAdapter(
    private val onEdit: (CategoryUi) -> Unit,
    private val onDelete: (CategoryUi) -> Unit
) : ListAdapter<CategoryUi, CategoryManageAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCategoryManageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCategoryManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryUi) {
            binding.tvEmoji.text = item.category.emoji
            binding.tvName.text = item.category.name
            binding.tvUsage.text = if (item.usageCount == 0L) {
                "Нет операций"
            } else {
                "${operationsCount(item.usageCount.toInt())} · ${Money.format(item.totalMinor)}"
            }
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
            binding.root.setOnClickListener { onEdit(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CategoryUi>() {
        override fun areItemsTheSame(oldItem: CategoryUi, newItem: CategoryUi): Boolean =
            oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(oldItem: CategoryUi, newItem: CategoryUi): Boolean =
            oldItem == newItem
    }
}
