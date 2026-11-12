package ru.fintracker.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.fintracker.R
import ru.fintracker.core.Dates
import ru.fintracker.core.Money
import ru.fintracker.data.model.FinanceSummary
import ru.fintracker.data.model.Period
import ru.fintracker.databinding.FragmentStatsBinding
import ru.fintracker.databinding.ItemStatCategoryBinding
import ru.fintracker.ui.common.color
import ru.fintracker.ui.common.collectFlow
import ru.fintracker.ui.common.showIf
import ru.fintracker.ui.common.tint
import ru.fintracker.ui.widget.BarChartView

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statsPeriodChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val period = when (checkedIds.firstOrNull()) {
                R.id.statsChipWeek -> Period.WEEK
                R.id.statsChipMonth -> Period.MONTH
                R.id.statsChipYear -> Period.YEAR
                R.id.statsChipAll -> Period.ALL
                else -> null
            }
            period?.let(viewModel::selectPeriod)
        }

        collectFlow(viewModel.period) {
            binding.tvStatsPeriodLabel.text = Dates.periodLabel(it)
        }
        collectFlow(viewModel.summary) { render(it) }
    }

    private fun render(summary: FinanceSummary) {
        binding.tvStatsExpense.text = Money.format(summary.expenseMinor)
        binding.tvStatsIncome.text = Money.format(summary.incomeMinor)
        binding.tvStatsBalance.text = Money.format(summary.balanceMinor)
        binding.tvStatsBalance.setTextColor(
            requireContext().color(
                if (summary.balanceMinor >= 0) R.color.income else R.color.expense
            )
        )
        binding.tvStatsCount.text = summary.transactionCount.toString()
        binding.tvStatsAverage.text = Money.format(summary.averagePerDayMinor)
        binding.tvStatsPeriodLabel.text = Dates.periodLabel(viewModel.period.value)

        renderTopCategories(summary)
        renderInsight(summary)

        binding.barChartStats.setData(
            summary.bars.map { BarChartView.Bar(it.label, it.incomeMinor, it.expenseMinor) }
        )
    }

    private fun renderTopCategories(summary: FinanceSummary) {
        val top = summary.expenseByCategory.take(6)
        binding.topCategoriesContainer.removeAllViews()
        binding.tvTopEmpty.showIf(top.isEmpty())
        binding.topCategoriesContainer.showIf(top.isNotEmpty())

        val max = top.firstOrNull()?.totalMinor ?: 0L
        top.forEach { slice ->
            val row = ItemStatCategoryBinding.inflate(
                layoutInflater,
                binding.topCategoriesContainer,
                false
            )
            row.dot.backgroundTintList = tint(slice.color)
            row.tvName.text = slice.name
            row.tvAmount.text = Money.format(slice.totalMinor)
            row.tvPercent.text = "${slice.percentOf(summary.expenseMinor)}%"
            row.progress.setIndicatorColor(slice.color)
            row.progress.setTrackColor(requireContext().color(R.color.surface_variant))
            row.progress.setProgressCompat(
                if (max == 0L) 0 else ((slice.totalMinor * 100) / max).toInt(),
                true
            )
            binding.topCategoriesContainer.addView(row.root)
        }
    }

    private fun renderInsight(summary: FinanceSummary) {
        val top = summary.expenseByCategory.firstOrNull()
        binding.tvInsight.text = if (top == null) {
            "Добавьте расходы, чтобы увидеть, на что уходит больше всего."
        } else {
            buildString {
                append("Больше всего вы тратите на «${top.name}» — ")
                append("${top.percentOf(summary.expenseMinor)}% расходов.")
                if (summary.averagePerTransactionMinor > 0L) {
                    append(" Средний чек — ${Money.format(summary.averagePerTransactionMinor)}.")
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
