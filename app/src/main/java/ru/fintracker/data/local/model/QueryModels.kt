package ru.fintracker.data.local.model

import androidx.room.ColumnInfo

data class TypeTotals(
    @ColumnInfo(name = "expense") val expense: Long = 0L,
    @ColumnInfo(name = "income") val income: Long = 0L,
    @ColumnInfo(name = "count") val count: Long = 0L
)

data class CategoryTotalRow(
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "categoryId") val categoryId: Long,
    @ColumnInfo(name = "total") val total: Long
)

data class DailyTotalRow(
    @ColumnInfo(name = "day") val day: String,
    @ColumnInfo(name = "expense") val expense: Long,
    @ColumnInfo(name = "income") val income: Long
)

data class CategoryUsage(
    @ColumnInfo(name = "categoryId") val categoryId: Long,
    @ColumnInfo(name = "count") val count: Long,
    @ColumnInfo(name = "total") val total: Long
)
