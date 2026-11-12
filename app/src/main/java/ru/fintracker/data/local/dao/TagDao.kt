package ru.fintracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.fintracker.data.local.entity.TagEntity
import ru.fintracker.data.local.entity.TransactionTagCrossRef

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkAll(refs: List<TransactionTagCrossRef>)

    @Query("DELETE FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun unlink(transactionId: Long)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT tagId FROM transaction_tags)")
    suspend fun deleteOrphanTags()
}
