package dev.hiarni.wifidisabler

import dev.hiarni.wifidisabler.core.GuardEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardEventTest {
    @Test fun `round-trips names with separators and unicode`() {
        val events = listOf(
            GuardEvent(1L, "semi;colon\nnewline", true),
            GuardEvent(2L, "Café 📶", false),
        )
        assertEquals(events, GuardEvent.decode(GuardEvent.encode(events)))
    }

    @Test fun `ignores corrupt lines`() {
        val decoded = GuardEvent.decode("garbage\n5;1;SG9tZQ==\nx;1;SG9tZQ==")
        assertEquals(listOf(GuardEvent(5L, "Home", true)), decoded)
    }

    @Test fun `empty input decodes to empty list`() = assertTrue(GuardEvent.decode(null).isEmpty())

    @Test fun `prepend keeps newest first and caps size`() {
        var list = emptyList<GuardEvent>()
        repeat(GuardEvent.MAX_ENTRIES + 5) { list = GuardEvent.prepend(GuardEvent(it.toLong(), "n$it", true), list) }
        assertEquals(GuardEvent.MAX_ENTRIES, list.size)
        assertEquals((GuardEvent.MAX_ENTRIES + 4).toLong(), list.first().timestamp)
    }
}
