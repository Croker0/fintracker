package ru.fintracker.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.local.model.TransactionWithTags
import ru.fintracker.data.model.TransactionType

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as FinTrackerApp).container.repository

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun load(id: Long): TransactionWithTags? = repository.getTransaction(id)

    suspend fun save(
        id: Long,
        amountMinor: Long,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: Long,
        tags: List<String>
    ): Result<Unit> = runCatching {
        if (id == 0L) {
            repository.addTransaction(amountMinor, type, categoryId, note, date, tags)
        } else {
            val updated = repository.updateTransaction(
                id, amountMinor, type, categoryId, note, date, tags
            )
            if (!updated) error("Операция не найдена")
        }
    }

    suspend fun delete(id: Long) = repository.deleteTransaction(id)
}
