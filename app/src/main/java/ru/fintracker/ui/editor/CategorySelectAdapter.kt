package ru.fintracker.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.fintracker.R
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.databinding.ItemCategorySelectBinding
import ru.fintracker.ui.common.color

class CategorySelectAdapter(
    private val onSelect: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategorySelectAdapter.ViewHolder>(Diff) {

    private var selectedId: Long? = null

    fun submit(items: List<CategoryEntity>, selected: Long?) {
        selectedId = selected
        submitList(items) { notifyDataSetChanged() }
    }

    fun select(id: Long?) {
        if (selectedId == id) return
        selectedId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCategorySelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).id == selectedId)
    }

    inner class ViewHolder(
        private val binding: ItemCategorySelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryEntity, selected: Boolean) {
            val context = binding.root.context
            binding.tvEmoji.text = category.emoji
            binding.tvName.text = category.name
            binding.card.strokeColor =
                context.color(if (selected) R.color.brand_primary else R.color.transparent)
            binding.card.setCardBackgroundColor(
                context.color(if (selected) R.color.primary_container else R.color.surface_variant)
            )
            binding.card.setOnClickListener {
                select(category.id)
                onSelect(category)
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean =
            oldItem == newItem
    }
}
