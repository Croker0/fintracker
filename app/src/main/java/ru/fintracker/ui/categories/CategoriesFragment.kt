package ru.fintracker.ui.categories

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.fintracker.R
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType
import ru.fintracker.databinding.DialogCategoryBinding
import ru.fintracker.databinding.FragmentCategoriesBinding
import ru.fintracker.ui.common.color
import ru.fintracker.ui.common.collectFlow
import ru.fintracker.ui.common.dp
import ru.fintracker.ui.common.tint

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels()
    private lateinit var adapter: CategoryManageAdapter

    private val emojis = listOf(
        "🍔", "🛒", "🚕", "🏠", "💊", "🎮", "👕", "📚",
        "🎁", "✈️", "💼", "💰", "📈", "🐾", "☕", "🧾"
    )

    private val paletteRes = listOf(
        R.color.cat_orange,
        R.color.cat_red,
        R.color.cat_blue,
        R.color.cat_violet,
        R.color.cat_teal,
        R.color.cat_green,
        R.color.cat_slate
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryManageAdapter(
            onEdit = { showCategoryDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        binding.fabAdd.setOnClickListener { showCategoryDialog(null) }

        collectFlow(viewModel.items) { adapter.submitList(it) }
    }

    private fun showCategoryDialog(existing: CategoryUi?) {
        val dialogBinding = DialogCategoryBinding.inflate(layoutInflater)
        val category = existing?.category

        var selectedEmoji = category?.emoji ?: emojis.first()
        var selectedColor = category?.color
            ?: requireContext().color(R.color.cat_orange)
        var selectedKind = category?.kind ?: TransactionType.EXPENSE

        dialogBinding.etName.setText(category?.name.orEmpty())

        val emojiViews = mutableListOf<Pair<TextView, String>>()
        emojis.forEach { emoji ->
            val option = TextView(requireContext()).apply {
                text = emoji
                textSize = 22f
                gravity = Gravity.CENTER
                isClickable = true
                background = ContextCompat
                    .getDrawable(requireContext(), R.drawable.bg_emoji_option)
                    ?.mutate()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = requireContext().dp(46)
                    height = requireContext().dp(46)
                    setMargins(requireContext().dp(2), requireContext().dp(2), requireContext().dp(2), requireContext().dp(2))
                }
            }
            option.setOnClickListener {
                selectedEmoji = emoji
                updateEmojiSelection(emojiViews, selectedEmoji)
            }
            emojiViews += option to emoji
            dialogBinding.emojiGrid.addView(option)
        }
        updateEmojiSelection(emojiViews, selectedEmoji)

        val colorValues = paletteRes.map { requireContext().color(it) }.toMutableList()
        if (selectedColor !in colorValues) colorValues.add(0, selectedColor)
        val colorViews = mutableListOf<Pair<MaterialCardView, Int>>()
        colorValues.forEach { color ->
            val swatch = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    requireContext().dp(34),
                    requireContext().dp(34)
                ).apply {
                    setMargins(requireContext().dp(3), 0, requireContext().dp(3), 0)
                }
                radius = requireContext().dp(17).toFloat()
                strokeWidth = requireContext().dp(2)
                cardElevation = 0f
                setCardBackgroundColor(color)
                isClickable = true
            }
            swatch.setOnClickListener {
                selectedColor = color
                updateColorSelection(colorViews, selectedColor)
            }
            colorViews += swatch to color
            dialogBinding.colorRow.addView(swatch)
        }
        updateColorSelection(colorViews, selectedColor)

        dialogBinding.toggleType.check(
            if (selectedKind == TransactionType.EXPENSE) R.id.btnTypeExpense else R.id.btnTypeIncome
        )
        dialogBinding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedKind = if (checkedId == R.id.btnTypeIncome) {
                    TransactionType.INCOME
                } else {
                    TransactionType.EXPENSE
                }
            }
        }

        if (existing != null && existing.usageCount > 0L) {
            dialogBinding.btnTypeExpense.isEnabled = false
            dialogBinding.btnTypeIncome.isEnabled = false
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Новая категория" else "Изменить категорию")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etName.text?.toString().orEmpty().trim()
                if (name.isEmpty()) {
                    dialogBinding.tilName.error = "Введите название"
                    return@setOnClickListener
                }
                dialogBinding.tilName.error = null
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.save(
                        existing?.category,
                        name,
                        selectedEmoji,
                        selectedColor,
                        selectedKind
                    )
                    if (result.isSuccess) {
                        dialog.dismiss()
                    } else {
                        dialogBinding.tilName.error =
                            result.exceptionOrNull()?.message ?: "Не удалось сохранить"
                    }
                }
            }
        }
        dialog.show()
    }

    private fun updateEmojiSelection(
        views: List<Pair<TextView, String>>,
        selected: String
    ) {
        views.forEach { (view, emoji) ->
            view.backgroundTintList = tint(
                requireContext().color(
                    if (emoji == selected) R.color.primary_container else R.color.surface_variant
                )
            )
        }
    }

    private fun updateColorSelection(
        views: List<Pair<MaterialCardView, Int>>,
        selected: Int
    ) {
        views.forEach { (view, color) ->
            view.strokeColor = if (color == selected) {
                requireContext().color(R.color.text_primary)
            } else {
                Color.TRANSPARENT
            }
        }
    }

    private fun confirmDelete(item: CategoryUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить категорию?")
            .setMessage("«${item.category.name}» будет удалена.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.delete(item)
                    if (result.isFailure) {
                        Snackbar.make(
                            binding.root,
                            result.exceptionOrNull()?.message ?: "Не удалось удалить",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        binding.rvCategories.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
