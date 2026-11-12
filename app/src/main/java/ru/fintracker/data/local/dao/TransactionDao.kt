package ru.fintracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.fintracker.data.local.entity.TransactionEntity
import ru.fintracker.data.local.model.CategoryTotalRow
import ru.fintracker.data.local.model.DailyTotalRow
import ru.fintracker.data.local.model.TransactionWithTags
import ru.fintracker.data.local.model.TypeTotals

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getWithTags(id: Long): TransactionWithTags?

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeAllWithTags(): Flow<List<TransactionWithTags>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionWithTags>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    suspend fun getAllWithTags(): List<TransactionWithTags>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expense,
               COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS income,
               COUNT(*) AS count
        FROM transactions
        WHERE date BETWEEN :from AND :to
        """
    )
    fun observeTotals(from: Long, to: Long): Flow<TypeTotals>

    @Query(
        """
        SELECT type, categoryId, COALESCE(SUM(amountMinor), 0) AS total
        FROM transactions
        WHERE date BETWEEN :from AND :to
        GROUP BY type, categoryId
        """
    )
    fun observeCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotalRow>>

    @Query(
        """
        SELECT day, expense, income FROM (
            SELECT strftime('%Y-%m-%d', date / 1000, 'unixepoch', 'localtime') AS day,
                   COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expense,
                   COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS income
            FROM transactions
            WHERE date BETWEEN :from AND :to
            GROUP BY day
        )
        WHERE day IS NOT NULL
        ORDER BY day
        """
    )
    fun observeDailyTotals(from: Long, to: Long): Flow<List<DailyTotalRow>>
}
