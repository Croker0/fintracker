package ru.fintracker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats rubles with grouping`() {
        assertEquals("1 450 ₽", Money.format(145_000).replace('\u00A0', ' '))
    }

    @Test
    fun `formats kopecks`() {
        assertEquals("1 250,50 ₽", Money.format(125_050).replace('\u00A0', ' '))
    }

    @Test
    fun `parses comma and spaces`() {
        assertEquals(125_050L, Money.parse("1 250,50"))
        assertEquals(145_000L, Money.parse("1450"))
        assertEquals(99_990L, Money.parse("999,9"))
    }

    @Test
    fun `rejects invalid amounts`() {
        assertNull(Money.parse(""))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse("0"))
        assertNull(Money.parse("-10"))
    }

    @Test
    fun `edit text round trip`() {
        assertEquals("1450", Money.editText(145_000))
        assertEquals("1250,5", Money.editText(125_050))
        assertEquals(125_050L, Money.parse(Money.editText(125_050)))
    }
}
