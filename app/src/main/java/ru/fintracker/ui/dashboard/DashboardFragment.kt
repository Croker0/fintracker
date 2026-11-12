package ru.fintracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import ru.fintracker.R
import ru.fintracker.core.Dates
import ru.fintracker.core.Money
import ru.fintracker.data.model.CategorySlice
import ru.fintracker.data.model.FinanceSummary
import ru.fintracker.data.model.Period
import ru.fintracker.databinding.FragmentDashboardBinding
import ru.fintracker.databinding.ItemLegendBinding
import ru.fintracker.ui.MainActivity
import ru.fintracker.ui.common.color
import ru.fintracker.ui.common.collectFlow
import ru.fintracker.ui.common.showIf
import ru.fintracker.ui.common.tint
import ru.fintracker.ui.editor.TransactionEditorActivity
import ru.fintracker.ui.transactions.TransactionAdapter
import ru.fintracker.ui.widget.BarChartView
import ru.fintracker.ui.widget.DonutChartView

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = Dates.todayLine()

        adapter = TransactionAdapter { row -> openEditor(row.item.transaction.id) }
        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter
        binding.rvRecent.itemAnimator = null

        binding.periodChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val period = when (checkedIds.firstOrNull()) {
                R.id.chipWeek -> Period.WEEK
                R.id.chipMonth -> Period.MONTH
                R.id.chipYear -> Period.YEAR
                R.id.chipAll -> Period.ALL
                else -> null
            }
            period?.let(viewModel::selectPeriod)
        }

        binding.fabAdd.setOnClickListener { openEditor(0L) }
        binding.btnEmptyAdd.setOnClickListener { openEditor(0L) }
        binding.btnAllTransactions.setOnClickListener {
            (requireActivity() as? MainActivity)?.selectTab(R.id.nav_transactions)
        }

        collectFlow(viewModel.summary) { renderSummary(it) }
        collectFlow(viewModel.recent) { rows ->
            adapter.submitList(rows)
            binding.emptyState.showIf(rows.isEmpty())
            binding.rvRecent.showIf(rows.isNotEmpty())
        }
    }

    private fun renderSummary(summary: FinanceSummary) {
        binding.tvBalance.text = Money.format(summary.balanceMinor)
        binding.tvIncome.text = Money.format(summary.incomeMinor)
        binding.tvExpense.text = Money.format(summary.expenseMinor)

        renderDonut(summary)

        binding.barChart.setData(
            summary.bars.map {
                BarChartView.Bar(it.label, it.incomeMinor, it.expenseMinor)
            }
        )
    }

    private fun renderDonut(summary: FinanceSummary) {
        val top = summary.expenseByCategory.take(5)
        val rest = summary.expenseByCategory.drop(5)
        val display = if (rest.isEmpty()) {
            top
        } else {
            top + CategorySlice(
                categoryId = 0L,
                name = "Другое",
                emoji = "",
                color = requireContext().color(R.color.legend_other),
                totalMinor = rest.sumOf { it.totalMinor }
            )
        }

        binding.donutChart.setData(
            display.map { DonutChartView.Slice(it.name, it.totalMinor, it.color) },
            title = "Расходы",
            value = Money.format(summary.expenseMinor)
        )

        binding.legendContainer.removeAllViews()
        if (display.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Нет расходов за период"
                setTextColor(requireContext().color(R.color.text_secondary))
                textSize = 12f
            }
            binding.legendContainer.addView(empty)
            return
        }

        display.forEach { slice ->
            val row = ItemLegendBinding.inflate(layoutInflater, binding.legendContainer, false)
            row.dot.backgroundTintList = tint(slice.color)
            row.tvLegendName.text = slice.name
            row.tvLegendValue.text = "${slice.percentOf(summary.expenseMinor)}%"
            binding.legendContainer.addView(row.root)
        }
    }

    private fun openEditor(id: Long) {
        startActivity(
            Intent(requireContext(), TransactionEditorActivity::class.java)
                .putExtra(TransactionEditorActivity.EXTRA_ID, id)
        )
    }

    override fun onDestroyView() {
        binding.rvRecent.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
