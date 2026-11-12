package ru.fintracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.fintracker.data.model.TransactionType

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "kind"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val emoji: String,
    val color: Int,
    val kind: TransactionType,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
