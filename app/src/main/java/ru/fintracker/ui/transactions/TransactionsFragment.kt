package ru.fintracker.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.fintracker.R
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType
import ru.fintracker.databinding.FragmentTransactionsBinding
import ru.fintracker.ui.common.collectFlow
import ru.fintracker.ui.common.showIf
import ru.fintracker.ui.editor.TransactionEditorActivity

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    private var categories: List<CategoryEntity> = emptyList()
    private var selectedCategoryId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionAdapter { row -> openEditor(row.item.transaction.id) }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setQuery(text?.toString().orEmpty())
        }

        binding.typeChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val type = when (checkedIds.firstOrNull()) {
                R.id.chipExpense -> TransactionType.EXPENSE
                R.id.chipIncome -> TransactionType.INCOME
                else -> null
            }
            viewModel.setType(type)
            val selected = categories.firstOrNull { it.id == selectedCategoryId }
            if (selected != null && type != null && selected.kind != type) {
                clearCategoryFilter()
            }
        }

        binding.fabAdd.setOnClickListener { openEditor(0L) }
        binding.btnEmptyAdd.setOnClickListener { openEditor(0L) }

        collectFlow(viewModel.categories) { renderCategoryDropdown(it) }
        collectFlow(viewModel.items) { items ->
            adapter.submitList(items)
            binding.emptyState.showIf(items.isEmpty())
            binding.rvTransactions.showIf(items.isNotEmpty())
        }
    }

    private fun renderCategoryDropdown(categories: List<CategoryEntity>) {
        this.categories = categories
        val labels = buildList {
            add("Все категории")
            categories.forEach { add("${it.emoji}  ${it.name}") }
        }
        val dropdownAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            labels
        )
        binding.etCategoryFilter.setAdapter(dropdownAdapter)
        binding.etCategoryFilter.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                clearCategoryFilter()
            } else {
                val category = categories[position - 1]
                selectedCategoryId = category.id
                binding.tilCategory.hint = category.name
                binding.etCategoryFilter.setText("", false)
                viewModel.setCategory(category.id)
            }
        }

        if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
            clearCategoryFilter()
        }
    }

    private fun clearCategoryFilter() {
        selectedCategoryId = null
        binding.tilCategory.hint = "Категория"
        binding.etCategoryFilter.setText("", false)
        viewModel.setCategory(null)
    }

    private fun openEditor(id: Long) {
        startActivity(
            Intent(requireContext(), TransactionEditorActivity::class.java)
                .putExtra(TransactionEditorActivity.EXTRA_ID, id)
        )
    }

    override fun onDestroyView() {
        binding.rvTransactions.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
