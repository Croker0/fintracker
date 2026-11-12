package ru.fintracker.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import ru.fintracker.data.local.AppDatabase
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.entity.TagEntity
import ru.fintracker.data.local.entity.TransactionEntity
import ru.fintracker.data.local.entity.TransactionTagCrossRef
import ru.fintracker.data.local.model.CategoryUsage
import ru.fintracker.data.local.model.TransactionWithTags
import ru.fintracker.data.model.FinanceSummary
import ru.fintracker.data.model.Period
import ru.fintracker.data.model.SummaryBuilder
import ru.fintracker.data.model.TransactionType
import ru.fintracker.data.seed.SeedData
import java.time.LocalDate
import java.util.Locale

data class ExportRow(
    val date: Long,
    val type: TransactionType,
    val category: String,
    val emoji: String,
    val amountMinor: Long,
    val note: String,
    val tags: List<String>
)

class FinanceRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val tagDao = database.tagDao()

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeCategoryUsage(): Flow<List<CategoryUsage>> = categoryDao.observeUsage()

    fun observeAllTransactions(): Flow<List<TransactionWithTags>> = transactionDao.observeAllWithTags()

    fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithTags>> =
        transactionDao.observeRecent(limit)

    fun observeSummary(period: Period): Flow<FinanceSummary> {
        val range = period.range()
        return combine(
            transactionDao.observeTotals(range.from, range.to),
            transactionDao.observeCategoryTotals(range.from, range.to),
            transactionDao.observeDailyTotals(range.from, range.to),
            categoryDao.observeAll()
        ) { totals, categoryTotals, dailyTotals, categories ->
            SummaryBuilder.build(period, totals, categoryTotals, dailyTotals, categories)
        }.flowOn(Dispatchers.Default)
    }

    suspend fun getTransaction(id: Long): TransactionWithTags? = transactionDao.getWithTags(id)

    suspend fun addTransaction(
        amountMinor: Long,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: Long,
        tags: List<String>
    ): Long = database.withTransaction {
        val id = transactionDao.insert(
            TransactionEntity(
                amountMinor = amountMinor,
                type = type,
                categoryId = categoryId,
                note = note.trim(),
                date = date
            )
        )
        replaceTags(id, tags)
        id
    }

    suspend fun updateTransaction(
        id: Long,
        amountMinor: Long,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: Long,
        tags: List<String>
    ): Boolean = database.withTransaction {
        val existing = transactionDao.getById(id) ?: return@withTransaction false
        transactionDao.update(
            existing.copy(
                amountMinor = amountMinor,
                type = type,
                categoryId = categoryId,
                note = note.trim(),
                date = date
            )
        )
        replaceTags(id, tags)
        true
    }

    suspend fun deleteTransaction(id: Long) = database.withTransaction {
        transactionDao.deleteById(id)
        tagDao.deleteOrphanTags()
    }

    suspend fun clearTransactions() = database.withTransaction {
        transactionDao.deleteAll()
        tagDao.deleteOrphanTags()
    }

    suspend fun allCategories(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun countTransactionsInCategory(categoryId: Long): Long =
        categoryDao.countTransactions(categoryId)

    suspend fun saveCategory(
        existing: CategoryEntity?,
        name: String,
        emoji: String,
        color: Int,
        kind: TransactionType
    ): Result<Unit> = runCatching {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Введите название категории" }

        val duplicate = categoryDao.findByName(cleanName, kind)
        if (duplicate != null && duplicate.id != existing?.id) {
            error("Категория с таким названием уже есть")
        }

        if (existing == null) {
            val nextOrder = (categoryDao.getAll().filter { it.kind == kind }
                .maxOfOrNull { it.sortOrder } ?: 0) + 1
            categoryDao.insert(
                CategoryEntity(
                    name = cleanName,
                    emoji = emoji,
                    color = color,
                    kind = kind,
                    isDefault = false,
                    sortOrder = nextOrder
                )
            )
        } else {
            if (existing.kind != kind && categoryDao.countTransactions(existing.id) > 0L) {
                error("Нельзя менять тип категории, пока в ней есть операции")
            }
            categoryDao.update(
                existing.copy(name = cleanName, emoji = emoji, color = color, kind = kind)
            )
        }
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Unit> = runCatching {
        if (category.isDefault) {
            error("Стандартную категорию нельзя удалить")
        }
        if (categoryDao.countTransactions(category.id) > 0L) {
            error("В категории есть операции — сначала перенесите их")
        }
        categoryDao.deleteById(category.id)
    }

    suspend fun exportRows(): List<ExportRow> {
        val categories = categoryDao.getAll().associateBy { it.id }
        return transactionDao.getAllWithTags()
            .sortedBy { it.transaction.date }
            .map { item ->
                val category = categories[item.transaction.categoryId]
                ExportRow(
                    date = item.transaction.date,
                    type = item.transaction.type,
                    category = category?.name ?: "Без категории",
                    emoji = category?.emoji ?: "",
                    amountMinor = item.transaction.amountMinor,
                    note = item.transaction.note,
                    tags = item.tags.map { it.name }
                )
            }
    }

    suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0L) {
            seedDemoData(force = false)
        }
    }

    suspend fun seedDemoData(force: Boolean) = database.withTransaction {
        if (force) {
            transactionDao.deleteAll()
            categoryDao.deleteAll()
            tagDao.deleteAll()
        }
        if (categoryDao.count() > 0L) return@withTransaction

        val categoryIds = mutableMapOf<String, Long>()
        SeedData.categories.forEach { seed ->
            categoryIds[seed.key] = categoryDao.insert(seed.toEntity())
        }

        SeedData.transactions(LocalDate.now()).forEach { seed ->
            val categoryId = categoryIds[seed.categoryKey] ?: return@forEach
            val transactionId = transactionDao.insert(
                TransactionEntity(
                    amountMinor = seed.amountMinor,
                    type = seed.type,
                    categoryId = categoryId,
                    note = seed.note,
                    date = seed.date
                )
            )
            attachTags(transactionId, seed.tags)
        }
    }

    private suspend fun replaceTags(transactionId: Long, tags: List<String>) {
        tagDao.unlink(transactionId)
        attachTags(transactionId, tags)
    }

    private suspend fun attachTags(transactionId: Long, tags: List<String>) {
        val tagIds = normalizeTags(tags).map { name ->
            val existing = tagDao.findByName(name)
            existing?.id ?: run {
                val inserted = tagDao.insert(TagEntity(name = name))
                if (inserted == -1L) tagDao.findByName(name)?.id else inserted
            }
        }.filterNotNull()

        if (tagIds.isNotEmpty()) {
            tagDao.linkAll(tagIds.map { TransactionTagCrossRef(transactionId, it) })
        }
    }

    private fun normalizeTags(tags: List<String>): List<String> =
        tags.map { it.trim().lowercase(Locale("ru", "RU")) }
            .filter { it.isNotEmpty() && it.length <= 24 }
            .distinct()
            .take(MAX_TAGS)

    private companion object {
        const val MAX_TAGS = 6
    }
}
