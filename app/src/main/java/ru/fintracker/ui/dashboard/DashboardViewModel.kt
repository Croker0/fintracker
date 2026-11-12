package ru.fintracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.model.FinanceSummary
import ru.fintracker.data.model.Period
import ru.fintracker.ui.transactions.TransactionListItem
import ru.fintracker.ui.transactions.TransactionListBuilder

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as FinTrackerApp).container.repository

    private val _period = MutableStateFlow(Period.MONTH)
    val period: StateFlow<Period> = _period.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<FinanceSummary> = _period
        .flatMapLatest { period -> repository.observeSummary(period) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceSummary.EMPTY)

    val recent: StateFlow<List<TransactionListItem.Row>> =
        combine(
            repository.observeRecentTransactions(6),
            repository.observeCategories()
        ) { transactions, categories ->
            TransactionListBuilder.rows(transactions, categories)
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPeriod(period: Period) {
        _period.value = period
    }
}
