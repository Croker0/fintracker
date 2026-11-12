package ru.fintracker.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {

    private val RU: Locale = Locale("ru", "RU")

    fun format(minor: Long, withCurrency: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(RU).apply {
            minimumFractionDigits = if (minor % 100L == 0L) 0 else 2
            maximumFractionDigits = 2
        }
        val value = formatter.format(BigDecimal.valueOf(minor, 2))
        return if (withCurrency) "$value ₽" else value
    }

    fun formatSigned(minor: Long, type: String): String {
        val sign = if (type == "INCOME") "+" else "−"
        return "$sign ${format(minor)}"
    }

    fun plain(minor: Long): String =
        BigDecimal.valueOf(minor, 2).setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
            .replace('.', ',')

    fun editText(minor: Long): String =
        BigDecimal.valueOf(minor, 2).stripTrailingZeros().toPlainString().replace('.', ',')

    fun parse(input: String?): Long? {
        if (input.isNullOrBlank()) return null
        val cleaned = input
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace("₽", "")
            .replace(",", ".")
            .trim()
        if (cleaned.isEmpty()) return null
        val value = cleaned.toBigDecimalOrNull() ?: return null
        if (value <= BigDecimal.ZERO) return null
        if (value > BigDecimal("1000000000")) return null
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
