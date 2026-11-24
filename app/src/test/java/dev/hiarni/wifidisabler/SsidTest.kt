package dev.hiarni.wifidisabler

import dev.hiarni.wifidisabler.core.Ssid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SsidTest {
    @Test fun `strips quotes from UTF-8 names`() = assertEquals("Home", Ssid.normalize("\"Home\""))

    @Test fun `keeps hex names as-is`() = assertEquals("48656c6c6f", Ssid.normalize("48656c6c6f"))

    @Test fun `unknown and empty map to null`() {
        assertNull(Ssid.normalize(Ssid.UNKNOWN))
        assertNull(Ssid.normalize(null))
        assertNull(Ssid.normalize(""))
        assertNull(Ssid.normalize("\"\""))
    }

    @Test fun `keeps inner quotes and spaces`() = assertEquals(" a \"b\" ", Ssid.normalize("\" a \"b\" \""))

    @Test fun `user input is trimmed and length-checked`() {
        assertEquals("Cafe Wi-Fi", Ssid.fromUserInput("  Cafe Wi-Fi \n"))
        assertNull(Ssid.fromUserInput("   "))
        assertNull(Ssid.fromUserInput("x".repeat(33)))
        assertEquals("x".repeat(32), Ssid.fromUserInput("x".repeat(32)))
    }

    @Test fun `matching is exact and case-sensitive`() {
        val blocked = setOf("Office", "Car")
        assertTrue(Ssid.isBlocked("Office", blocked))
        assertFalse(Ssid.isBlocked("office", blocked))
        assertFalse(Ssid.isBlocked(null, blocked))
    }
}
