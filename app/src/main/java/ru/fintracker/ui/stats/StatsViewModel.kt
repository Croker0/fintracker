package ru.fintracker.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.model.FinanceSummary
import ru.fintracker.data.model.Period

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as FinTrackerApp).container.repository

    private val _period = MutableStateFlow(Period.MONTH)
    val period: StateFlow<Period> = _period.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<FinanceSummary> = _period
        .flatMapLatest { repository.observeSummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceSummary.EMPTY)

    fun selectPeriod(period: Period) {
        _period.value = period
    }
}
