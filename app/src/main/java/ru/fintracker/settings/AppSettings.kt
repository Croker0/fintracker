package ru.fintracker.settings

data class AppSettings(
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val autoSync: Boolean = true,
    val lastSyncAt: Long = 0L,
    val seeded: Boolean = false
)
