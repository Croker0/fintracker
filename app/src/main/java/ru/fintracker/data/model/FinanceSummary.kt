package ru.fintracker.data.model

import kotlin.math.roundToInt

data class CategorySlice(
    val categoryId: Long,
    val name: String,
    val emoji: String,
    val color: Int,
    val totalMinor: Long
) {
    fun percentOf(total: Long): Int =
        if (total <= 0L) 0 else ((totalMinor * 100.0) / total).roundToInt()
}

data class BarPoint(
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)

data class FinanceSummary(
    val incomeMinor: Long = 0L,
    val expenseMinor: Long = 0L,
    val transactionCount: Int = 0,
    val averagePerDayMinor: Long = 0L,
    val expenseByCategory: List<CategorySlice> = emptyList(),
    val incomeByCategory: List<CategorySlice> = emptyList(),
    val bars: List<BarPoint> = emptyList()
) {
    val balanceMinor: Long get() = incomeMinor - expenseMinor

    val averagePerTransactionMinor: Long
        get() = if (transactionCount == 0) 0L else expenseMinor / transactionCount

    companion object {
        val EMPTY = FinanceSummary()
    }
}
