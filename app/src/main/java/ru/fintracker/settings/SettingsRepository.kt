package ru.fintracker.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) = update { it.copy(reminderEnabled = enabled) }

    fun setReminderTime(hour: Int, minute: Int) =
        update { it.copy(reminderHour = hour, reminderMinute = minute) }

    fun setAutoSync(enabled: Boolean) = update { it.copy(autoSync = enabled) }

    fun setLastSync(timestamp: Long) = update { it.copy(lastSyncAt = timestamp) }

    fun setSeeded(value: Boolean) = update { it.copy(seeded = value) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_state.value)
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, next.reminderEnabled)
            .putInt(KEY_REMINDER_HOUR, next.reminderHour)
            .putInt(KEY_REMINDER_MINUTE, next.reminderMinute)
            .putBoolean(KEY_AUTO_SYNC, next.autoSync)
            .putLong(KEY_LAST_SYNC, next.lastSyncAt)
            .putBoolean(KEY_SEEDED, next.seeded)
            .apply()
        _state.value = next
    }

    private fun load(): AppSettings = AppSettings(
        reminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true),
        reminderHour = prefs.getInt(KEY_REMINDER_HOUR, 20),
        reminderMinute = prefs.getInt(KEY_REMINDER_MINUTE, 0),
        autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true),
        lastSyncAt = prefs.getLong(KEY_LAST_SYNC, 0L),
        seeded = prefs.getBoolean(KEY_SEEDED, false)
    )

    private companion object {
        const val PREFS_NAME = "fintracker_settings"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_AUTO_SYNC = "auto_sync"
        const val KEY_LAST_SYNC = "last_sync"
        const val KEY_SEEDED = "seeded"
    }
}
