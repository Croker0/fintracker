package ru.fintracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.model.CategoryTotalRow
import ru.fintracker.data.local.model.DailyTotalRow
import ru.fintracker.data.local.model.TypeTotals
import java.time.LocalDate

class SummaryBuilderTest {

    private val today = LocalDate.of(2026, 11, 13)

    private val categories = listOf(
        CategoryEntity(1L, "Продукты", "🛒", 0xFFF97316.toInt(), TransactionType.EXPENSE, true, 1),
        CategoryEntity(2L, "Кафе", "☕", 0xFFEF4444.toInt(), TransactionType.EXPENSE, true, 2),
        CategoryEntity(3L, "Зарплата", "💼", 0xFF16A34A.toInt(), TransactionType.INCOME, true, 1)
    )

    private val totals = TypeTotals(expense = 300_000, income = 850_000, count = 4)

    private val categoryTotals = listOf(
        CategoryTotalRow("EXPENSE", 1L, 200_000),
        CategoryTotalRow("EXPENSE", 2L, 100_000),
        CategoryTotalRow("INCOME", 3L, 850_000)
    )

    private val daily = listOf(
        DailyTotalRow("2026-11-11", expense = 100_000, income = 0),
        DailyTotalRow("2026-11-12", expense = 50_000, income = 0),
        DailyTotalRow("2026-11-13", expense = 150_000, income = 850_000)
    )

    @Test
    fun `month summary keeps totals and sorts categories`() {
        val summary = SummaryBuilder.build(
            Period.MONTH, totals, categoryTotals, daily, categories, today
        )

        assertEquals(300_000L, summary.expenseMinor)
        assertEquals(850_000L, summary.incomeMinor)
        assertEquals(550_000L, summary.balanceMinor)
        assertEquals(4, summary.transactionCount)
        assertEquals("Продукты", summary.expenseByCategory.first().name)
        assertEquals(200_000L, summary.expenseByCategory.first().totalMinor)
        assertEquals(67, summary.expenseByCategory.first().percentOf(summary.expenseMinor))
        assertEquals("Зарплата", summary.incomeByCategory.first().name)
    }

    @Test
    fun `month bars cover every day up to today`() {
        val summary = SummaryBuilder.build(
            Period.MONTH, totals, categoryTotals, daily, categories, today
        )

        assertEquals(13, summary.bars.size)
        assertEquals("13", summary.bars.last().label)
        assertEquals(850_000L, summary.bars.last().incomeMinor)
        assertEquals(150_000L, summary.bars.last().expenseMinor)
        assertEquals(0L, summary.bars.first().expenseMinor)
    }

    @Test
    fun `week bars have seven points`() {
        val summary = SummaryBuilder.build(
            Period.WEEK, totals, categoryTotals, daily, categories, today
        )
        assertEquals(7, summary.bars.size)
    }

    @Test
    fun `all period builds trailing twelve months`() {
        val summary = SummaryBuilder.build(
            Period.ALL, totals, categoryTotals, daily, categories, today
        )
        assertEquals(12, summary.bars.size)
    }

    @Test
    fun `average per day never divides by zero`() {
        val empty = SummaryBuilder.build(
            Period.MONTH,
            TypeTotals(),
            emptyList(),
            emptyList(),
            categories,
            today
        )
        assertEquals(0L, empty.expenseMinor)
        assertEquals(0L, empty.averagePerDayMinor)
        assertTrue(empty.expenseByCategory.isEmpty())
    }

    @Test
    fun `period ranges contain today`() {
        val range = Period.MONTH.range(today)
        assertTrue(range.from < range.to)
        assertEquals(13, Period.MONTH.dayCount(today))
    }
}
