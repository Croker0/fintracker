package ru.fintracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.fintracker.FinTrackerApp

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? FinTrackerApp ?: return Result.success()
        if (!app.container.settings.state.value.reminderEnabled) return Result.success()
        NotificationHelper.showReminder(applicationContext)
        return Result.success()
    }
}
