package ru.fintracker.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType

class CategoriesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as FinTrackerApp).container.repository

    val items: StateFlow<List<CategoryUi>> = combine(
        repository.observeCategories(),
        repository.observeCategoryUsage()
    ) { categories, usage ->
        val usageById = usage.associateBy { it.categoryId }
        categories.map { category ->
            val row = usageById[category.id]
            CategoryUi(
                category = category,
                usageCount = row?.count ?: 0L,
                totalMinor = row?.total ?: 0L
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun save(
        existing: CategoryEntity?,
        name: String,
        emoji: String,
        color: Int,
        kind: TransactionType
    ): Result<Unit> = repository.saveCategory(existing, name, emoji, color, kind)

    suspend fun delete(item: CategoryUi): Result<Unit> =
        repository.deleteCategory(item.category)
}
