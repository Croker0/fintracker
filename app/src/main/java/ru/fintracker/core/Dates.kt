package ru.fintracker.core

import ru.fintracker.data.model.Period
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {

    private val RU: Locale = Locale("ru", "RU")
    private val FULL_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", RU)
    private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", RU)
    private val MONTH_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", RU)
    private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", RU)
    private val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", RU)

    fun todayLine(today: LocalDate = LocalDate.now()): String =
        "Сегодня, ${today.format(DAY_MONTH)}"

    fun dayHeader(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> date.format(FULL_DATE)
    }

    fun fullDate(date: LocalDate): String = date.format(FULL_DATE)

    fun time(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(TIME)

    fun dateTime(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DATE_TIME)

    fun periodLabel(period: Period, today: LocalDate = LocalDate.now()): String = when (period) {
        Period.WEEK -> "Последние 7 дней"
        Period.MONTH -> today.format(MONTH_YEAR).replaceFirstChar { it.titlecase(RU) }
        Period.YEAR -> "${today.year} год"
        Period.ALL -> "Всё время"
    }

    /**
     * Keeps the original time-of-day when the user edits only the amount/note,
     * uses the current time for today's new entries and noon for backdated ones.
     */
    fun storeMillis(
        date: LocalDate,
        originalMillis: Long? = null,
        now: Long = System.currentTimeMillis()
    ): Long {
        val zone = ZoneId.systemDefault()
        if (originalMillis != null) {
            val originalDate = Instant.ofEpochMilli(originalMillis).atZone(zone).toLocalDate()
            if (originalDate == date) return originalMillis
        }
        val time = if (date == LocalDate.now()) {
            Instant.ofEpochMilli(now).atZone(zone).toLocalTime()
        } else {
            LocalTime.NOON
        }
        return date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }
}
