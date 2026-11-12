package ru.fintracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.model.CategoryUsage
import ru.fintracker.data.model.TransactionType

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE AND kind = :kind LIMIT 1")
    suspend fun findByName(name: String, kind: TransactionType): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactions(categoryId: Long): Long

    @Query(
        """
        SELECT categoryId, COUNT(*) AS count, COALESCE(SUM(amountMinor), 0) AS total
        FROM transactions
        GROUP BY categoryId
        """
    )
    fun observeUsage(): Flow<List<CategoryUsage>>
}
