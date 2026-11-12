package ru.fintracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import ru.fintracker.FinTrackerApp
import ru.fintracker.data.export.BackupManager

/**
 * Simulates a background sync: builds a local JSON snapshot and records the timestamp.
 * The worker is scheduled by WorkManager with a network constraint.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? FinTrackerApp ?: return Result.success()
        return runCatching {
            delay(600)
            BackupManager.writeLocalBackup(applicationContext, app.container.repository)
            app.container.settings.setLastSync(System.currentTimeMillis())
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
