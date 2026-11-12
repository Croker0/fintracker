package ru.fintracker.ui.transactions

import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.model.TransactionWithTags
import ru.fintracker.data.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

sealed interface TransactionListItem {

    data class Header(
        val date: LocalDate,
        val expenseMinor: Long,
        val incomeMinor: Long
    ) : TransactionListItem

    data class Row(
        val item: TransactionWithTags,
        val category: CategoryEntity?
    ) : TransactionListItem

    data class Footer(
        val expenseMinor: Long,
        val incomeMinor: Long,
        val count: Int
    ) : TransactionListItem
}

object TransactionListBuilder {

    fun build(
        transactions: List<TransactionWithTags>,
        categories: List<CategoryEntity>,
        query: String = "",
        type: TransactionType? = null,
        categoryId: Long? = null
    ): List<TransactionListItem> {
        if (transactions.isEmpty()) return emptyList()

        val categoryById = categories.associateBy { it.id }
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)

        val filtered = transactions.filter { item ->
            val transaction = item.transaction
            val category = categoryById[transaction.categoryId]
            val matchesType = type == null || transaction.type == type
            val matchesCategory = categoryId == null || transaction.categoryId == categoryId
            val matchesQuery = normalizedQuery.isEmpty() ||
                transaction.note.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                category?.name?.lowercase(Locale.ROOT)?.contains(normalizedQuery) == true ||
                item.tags.any { it.name.lowercase(Locale.ROOT).contains(normalizedQuery) }
            matchesType && matchesCategory && matchesQuery
        }
        if (filtered.isEmpty()) return emptyList()

        val zone = ZoneId.systemDefault()
        val groups = filtered.groupBy {
            Instant.ofEpochMilli(it.transaction.date).atZone(zone).toLocalDate()
        }

        val items = mutableListOf<TransactionListItem>()
        var totalIncome = 0L
        var totalExpense = 0L

        groups.entries.sortedByDescending { it.key }.forEach { (date, dayTransactions) ->
            val dayIncome = dayTransactions
                .filter { it.transaction.type == TransactionType.INCOME }
                .sumOf { it.transaction.amountMinor }
            val dayExpense = dayTransactions
                .filter { it.transaction.type == TransactionType.EXPENSE }
                .sumOf { it.transaction.amountMinor }
            totalIncome += dayIncome
            totalExpense += dayExpense

            items += TransactionListItem.Header(date, dayExpense, dayIncome)
            dayTransactions
                .sortedByDescending { it.transaction.date }
                .forEach { item ->
                    items += TransactionListItem.Row(item, categoryById[item.transaction.categoryId])
                }
        }

        items += TransactionListItem.Footer(
            expenseMinor = totalExpense,
            incomeMinor = totalIncome,
            count = filtered.size
        )
        return items
    }

    fun rows(
        transactions: List<TransactionWithTags>,
        categories: List<CategoryEntity>
    ): List<TransactionListItem.Row> {
        val categoryById = categories.associateBy { it.id }
        return transactions.map { item ->
            TransactionListItem.Row(item, categoryById[item.transaction.categoryId])
        }
    }
}
