package ru.fintracker.ui.editor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import ru.fintracker.R
import ru.fintracker.core.Dates
import ru.fintracker.core.Money
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType
import ru.fintracker.databinding.ActivityEditorBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class TransactionEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_transaction_id"
    }

    private lateinit var binding: ActivityEditorBinding
    private val viewModel: EditorViewModel by viewModels()

    private lateinit var categoryAdapter: CategorySelectAdapter

    private var editingId: Long = 0L
    private var originalDateMillis: Long? = null
    private var type: TransactionType = TransactionType.EXPENSE
    private var selectedCategoryId: Long? = null
    private var selectedDate: LocalDate = LocalDate.now()
    private var allCategories: List<CategoryEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getLongExtra(EXTRA_ID, 0L)

        setupToolbar()
        setupCategoryGrid()
        setupTypeToggle()
        setupDateField()
        setupTags()

        binding.btnSave.setOnClickListener { save() }

        observeCategories()

        if (editingId != 0L) {
            loadTransaction()
        } else {
            updateDateLabel()
            binding.toolbar.menu.findItem(R.id.action_delete)?.isVisible = false
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (editingId == 0L) "Новая операция" else "Редактировать"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                confirmDelete()
                true
            } else {
                false
            }
        }
        binding.toolbar.menu.findItem(R.id.action_delete)?.isVisible = editingId != 0L
    }

    private fun setupCategoryGrid() {
        categoryAdapter = CategorySelectAdapter { category ->
            selectedCategoryId = category.id
        }
        binding.rvCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupTypeToggle() {
        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            type = if (checkedId == R.id.btnIncome) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
            val selected = allCategories.firstOrNull { it.id == selectedCategoryId }
            if (selected != null && selected.kind != type) {
                selectedCategoryId = null
            }
            renderCategories()
        }
    }

    private fun setupDateField() {
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun setupTags() {
        binding.etTags.doAfterTextChanged { text ->
            renderTagsPreview(text?.toString().orEmpty())
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    allCategories = categories
                    renderCategories()
                }
            }
        }
    }

    private fun renderCategories() {
        val filtered = allCategories.filter { it.kind == type }
        if (allCategories.isNotEmpty() &&
            selectedCategoryId != null &&
            filtered.none { it.id == selectedCategoryId }
        ) {
            selectedCategoryId = null
        }
        categoryAdapter.submit(filtered, selectedCategoryId)
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            val data = viewModel.load(editingId)
            if (data == null) {
                Toast.makeText(this@TransactionEditorActivity, "Операция не найдена", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            val transaction = data.transaction
            originalDateMillis = transaction.date
            type = transaction.type
            selectedCategoryId = transaction.categoryId
            selectedDate = Instant.ofEpochMilli(transaction.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            binding.toggleType.check(
                if (type == TransactionType.EXPENSE) R.id.btnExpense else R.id.btnIncome
            )
            binding.etAmount.setText(Money.editText(transaction.amountMinor))
            binding.etNote.setText(transaction.note)
            binding.etTags.setText(data.tags.joinToString(", ") { it.name })
            binding.etTags.setSelection(binding.etTags.text?.length ?: 0)
            updateDateLabel()
            renderCategories()
        }
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Дата операции")
            .setSelection(selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            updateDateLabel()
        }
        picker.show(supportFragmentManager, "transaction_date")
    }

    private fun updateDateLabel() {
        binding.etDate.setText(Dates.fullDate(selectedDate))
    }

    private fun renderTagsPreview(raw: String) {
        binding.tagsPreview.removeAllViews()
        parseTags(raw).forEach { tag ->
            val chip = Chip(this).apply {
                text = tag
                isCheckable = false
                isClickable = false
                textSize = 11f
                chipMinHeight = 30f * resources.displayMetrics.density
            }
            binding.tagsPreview.addView(chip)
        }
    }

    private fun parseTags(raw: String): List<String> =
        raw.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(6)

    private fun save() {
        val amount = Money.parse(binding.etAmount.text?.toString())
        if (amount == null) {
            binding.tilAmount.error = "Введите сумму больше нуля"
            return
        }
        binding.tilAmount.error = null

        val categoryId = selectedCategoryId
        if (categoryId == null) {
            Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show()
            return
        }

        val dateMillis = Dates.storeMillis(selectedDate, originalDateMillis)
        val note = binding.etNote.text?.toString().orEmpty().trim()
        val tags = parseTags(binding.etTags.text?.toString().orEmpty())

        binding.btnSave.isEnabled = false
        lifecycleScope.launch {
            val result = viewModel.save(
                id = editingId,
                amountMinor = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                date = dateMillis,
                tags = tags
            )
            if (result.isSuccess) {
                finish()
            } else {
                binding.btnSave.isEnabled = true
                Toast.makeText(
                    this@TransactionEditorActivity,
                    result.exceptionOrNull()?.message ?: "Не удалось сохранить",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun confirmDelete() {
        if (editingId == 0L) return
        MaterialAlertDialogBuilder(this)
            .setTitle("Удалить операцию?")
            .setMessage("Это действие нельзя отменить.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    viewModel.delete(editingId)
                    finish()
                }
            }
            .show()
    }
}
