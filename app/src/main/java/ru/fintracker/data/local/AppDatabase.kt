package ru.fintracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.fintracker.data.local.dao.CategoryDao
import ru.fintracker.data.local.dao.TagDao
import ru.fintracker.data.local.dao.TransactionDao
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.entity.TagEntity
import ru.fintracker.data.local.entity.TransactionEntity
import ru.fintracker.data.local.entity.TransactionTagCrossRef

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun tagDao(): TagDao

    companion object {
        private const val DB_NAME = "fintracker.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
