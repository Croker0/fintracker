package ru.fintracker.ui.transactions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.entity.TagEntity
import ru.fintracker.data.local.entity.TransactionEntity
import ru.fintracker.data.local.model.TransactionWithTags
import ru.fintracker.data.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId

class TransactionListBuilderTest {

    private val zone = ZoneId.systemDefault()

    private fun millis(date: LocalDate, hour: Int = 12): Long =
        date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private val categories = listOf(
        CategoryEntity(1L, "Продукты", "🛒", 0xFFF97316.toInt(), TransactionType.EXPENSE, true, 1),
        CategoryEntity(2L, "Зарплата", "💼", 0xFF16A34A.toInt(), TransactionType.INCOME, true, 1)
    )

    private fun tx(
        id: Long,
        amount: Long,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: LocalDate,
        tags: List<String> = emptyList()
    ): TransactionWithTags = TransactionWithTags(
        transaction = TransactionEntity(
            id = id,
            amountMinor = amount,
            type = type,
            categoryId = categoryId,
            note = note,
            date = millis(date)
        ),
        tags = tags.mapIndexed { index, name -> TagEntity(index.toLong() + 1, name) }
    )

    private val transactions = listOf(
        tx(2, 85_000_00, TransactionType.INCOME, 2, "Зарплата", LocalDate.of(2026, 11, 13), listOf("работа")),
        tx(1, 1_450_00, TransactionType.EXPENSE, 1, "Пятёрочка", LocalDate.of(2026, 11, 13)),
        tx(3, 320_00, TransactionType.EXPENSE, 1, "Кофе", LocalDate.of(2026, 11, 12), listOf("утро"))
    )

    @Test
    fun `groups transactions by day and adds totals`() {
        val items = TransactionListBuilder.build(transactions, categories)

        assertEquals(6, items.size)
        assertTrue(items[0] is TransactionListItem.Header)
        assertTrue(items[1] is TransactionListItem.Row)
        assertTrue(items[2] is TransactionListItem.Row)
        assertTrue(items[3] is TransactionListItem.Header)
        val footer = items.last() as TransactionListItem.Footer
        assertEquals(3, footer.count)
        assertEquals(1_770_00L, footer.expenseMinor)
        assertEquals(85_000_00L, footer.incomeMinor)
    }

    @Test
    fun `filters by query note and tag`() {
        val byNote = TransactionListBuilder.build(transactions, categories, query = "кофе")
        assertEquals(1, (byNote.last() as TransactionListItem.Footer).count)

        val byTag = TransactionListBuilder.build(transactions, categories, query = "работа")
        assertEquals(1, (byTag.last() as TransactionListItem.Footer).count)
    }

    @Test
    fun `filters by type and category`() {
        val expenses = TransactionListBuilder.build(
            transactions,
            categories,
            type = TransactionType.EXPENSE
        )
        assertEquals(2, (expenses.last() as TransactionListItem.Footer).count)

        val salary = TransactionListBuilder.build(transactions, categories, categoryId = 2L)
        assertEquals(1, (salary.last() as TransactionListItem.Footer).count)
    }

    @Test
    fun `empty filter returns empty list`() {
        val items = TransactionListBuilder.build(transactions, categories, query = "нет такого")
        assertTrue(items.isEmpty())
    }
}
