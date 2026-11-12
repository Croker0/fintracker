package ru.fintracker.data.model

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class DateRange(val from: Long, val to: Long)

enum class Period(val label: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год"),
    ALL("Всё время");

    fun range(today: LocalDate = LocalDate.now()): DateRange {
        val zone = ZoneId.systemDefault()
        val start = when (this) {
            WEEK -> today.minusDays(6)
            MONTH -> today.withDayOfMonth(1)
            YEAR -> today.withDayOfYear(1)
            ALL -> LocalDate.of(1970, 1, 1)
        }
        val endExclusive = today.plusDays(1)
        return DateRange(
            from = start.atStartOfDay(zone).toInstant().toEpochMilli(),
            to = endExclusive.atStartOfDay(zone).toInstant().toEpochMilli() - 1
        )
    }

    fun dayCount(today: LocalDate = LocalDate.now()): Long = when (this) {
        WEEK -> 7
        MONTH -> today.dayOfMonth.toLong()
        YEAR -> today.dayOfYear.toLong()
        ALL -> ChronoUnit.DAYS.between(LocalDate.of(1970, 1, 1), today) + 1
    }
}
