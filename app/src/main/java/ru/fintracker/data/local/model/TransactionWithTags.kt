package ru.fintracker.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.fintracker.data.local.entity.TagEntity
import ru.fintracker.data.local.entity.TransactionEntity
import ru.fintracker.data.local.entity.TransactionTagCrossRef

data class TransactionWithTags(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity> = emptyList()
)
