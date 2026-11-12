package ru.fintracker.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType

class TransactionsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as FinTrackerApp).container.repository

    private val query = MutableStateFlow("")
    private val type = MutableStateFlow<TransactionType?>(null)
    private val categoryId = MutableStateFlow<Long?>(null)

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items: StateFlow<List<TransactionListItem>> = combine(
        repository.observeAllTransactions(),
        repository.observeCategories(),
        query,
        type,
        categoryId
    ) { transactions, categories, query, type, categoryId ->
        TransactionListBuilder.build(transactions, categories, query, type, categoryId)
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setType(value: TransactionType?) {
        type.value = value
    }

    fun setCategory(value: Long?) {
        categoryId.value = value
    }
}
