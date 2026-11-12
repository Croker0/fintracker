package ru.fintracker.data.model

import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.model.CategoryTotalRow
import ru.fintracker.data.local.model.DailyTotalRow
import ru.fintracker.data.local.model.TypeTotals
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Pure aggregator: turns SQL rows into chart-ready models.
 * Kept free of Android APIs so it can be covered with local unit tests.
 */
object SummaryBuilder {

    private val RU: Locale = Locale("ru", "RU")
    private val FALLBACK_COLOR = 0xFF98A2B3.toInt()

    fun build(
        period: Period,
        totals: TypeTotals,
        categoryTotals: List<CategoryTotalRow>,
        dailyTotals: List<DailyTotalRow>,
        categories: List<CategoryEntity>,
        today: LocalDate = LocalDate.now()
    ): FinanceSummary {
        val average = averagePerDay(period, dailyTotals, totals.expense, today)
        return FinanceSummary(
            incomeMinor = totals.income,
            expenseMinor = totals.expense,
            transactionCount = totals.count.toInt(),
            averagePerDayMinor = average,
            expenseByCategory = slices(TransactionType.EXPENSE, categoryTotals, categories),
            incomeByCategory = slices(TransactionType.INCOME, categoryTotals, categories),
            bars = buildBars(period, dailyTotals, today)
        )
    }

    private fun slices(
        type: TransactionType,
        rows: List<CategoryTotalRow>,
        categories: List<CategoryEntity>
    ): List<CategorySlice> {
        val byId = categories.associateBy { it.id }
        return rows.asSequence()
            .filter { it.type == type.name && it.total > 0L }
            .map { row ->
                val category = byId[row.categoryId]
                CategorySlice(
                    categoryId = row.categoryId,
                    name = category?.name ?: "Без категории",
                    emoji = category?.emoji ?: "❓",
                    color = category?.color ?: FALLBACK_COLOR,
                    totalMinor = row.total
                )
            }
            .sortedByDescending { it.totalMinor }
            .toList()
    }

    private fun averagePerDay(
        period: Period,
        daily: List<DailyTotalRow>,
        expense: Long,
        today: LocalDate
    ): Long {
        if (expense <= 0L) return 0L
        val start = when (period) {
            Period.WEEK -> today.minusDays(6)
            Period.MONTH -> today.withDayOfMonth(1)
            Period.YEAR -> today.withDayOfYear(1)
            Period.ALL -> daily.mapNotNull { parseDate(it.day) }.minOrNull() ?: today
        }
        val days = (ChronoUnit.DAYS.between(start, today) + 1).coerceAtLeast(1)
        return expense / days
    }

    private fun buildBars(
        period: Period,
        daily: List<DailyTotalRow>,
        today: LocalDate
    ): List<BarPoint> {
        val rows = daily.mapNotNull { row -> parseDate(row.day)?.let { it to row } }
        val byDay = rows.associate { it.first to it.second }
        val byMonth = rows.groupBy { YearMonth.from(it.first) }.mapValues { entry ->
            entry.value.fold(0L to 0L) { acc, item ->
                (acc.first + item.second.income) to (acc.second + item.second.expense)
            }
        }

        fun dayPoint(date: LocalDate): BarPoint {
            val row = byDay[date]
            return BarPoint(
                label = dayLabel(period, date),
                incomeMinor = row?.income ?: 0L,
                expenseMinor = row?.expense ?: 0L
            )
        }

        fun monthPoint(month: YearMonth): BarPoint {
            val sums = byMonth[month] ?: (0L to 0L)
            return BarPoint(
                label = month.month.getDisplayName(TextStyle.SHORT, RU).removeSuffix("."),
                incomeMinor = sums.first,
                expenseMinor = sums.second
            )
        }

        return when (period) {
            Period.WEEK -> (6 downTo 0).map { dayPoint(today.minusDays(it.toLong())) }
            Period.MONTH -> (1..today.dayOfMonth).map { dayPoint(today.withDayOfMonth(it)) }
            Period.YEAR -> (1..today.monthValue).map { monthPoint(YearMonth.of(today.year, it)) }
            Period.ALL -> (11 downTo 0).map {
                monthPoint(YearMonth.from(today).minusMonths(it.toLong()))
            }
        }
    }

    private fun dayLabel(period: Period, date: LocalDate): String =
        if (period == Period.WEEK) {
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, RU).removeSuffix(".")
        } else {
            date.dayOfMonth.toString()
        }

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()
}
