package ru.fintracker

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.fintracker.data.local.AppDatabase
import ru.fintracker.data.repository.FinanceRepository
import ru.fintracker.settings.SettingsRepository
import ru.fintracker.work.NotificationHelper
import ru.fintracker.work.WorkScheduler

class AppContainer(context: Application) {
    val database: AppDatabase = AppDatabase.create(context)
    val repository: FinanceRepository = FinanceRepository(database)
    val settings: SettingsRepository = SettingsRepository(context)
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class FinTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
        WorkScheduler.applyAll(this, container.settings.state.value)

        container.applicationScope.launch {
            if (!container.settings.state.value.seeded) {
                container.repository.seedIfEmpty()
                container.settings.setSeeded(true)
            }
        }
    }
}
