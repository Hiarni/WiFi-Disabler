package dev.hiarni.wifidisabler.core

import java.util.Base64

/** One entry of the local activity log. Never leaves the device. */
data class GuardEvent(
    val timestamp: Long,
    val ssid: String,
    val success: Boolean,
) {
    companion object {
        const val MAX_ENTRIES = 20

        fun encode(events: List<GuardEvent>): String =
            events.take(MAX_ENTRIES).joinToString("\n") { e ->
                val name = Base64.getEncoder().encodeToString(e.ssid.toByteArray(Charsets.UTF_8))
                "${e.timestamp};${if (e.success) 1 else 0};$name"
            }

        fun decode(raw: String?): List<GuardEvent> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.lineSequence().mapNotNull { line ->
                val parts = line.split(';')
                if (parts.size != 3) return@mapNotNull null
                val time = parts[0].toLongOrNull() ?: return@mapNotNull null
                val name = runCatching {
                    String(Base64.getDecoder().decode(parts[2]), Charsets.UTF_8)
                }.getOrNull() ?: return@mapNotNull null
                GuardEvent(time, name, parts[1] == "1")
            }.toList()
        }

        /** Newest first, capped at [MAX_ENTRIES]. */
        fun prepend(event: GuardEvent, existing: List<GuardEvent>): List<GuardEvent> =
            (listOf(event) + existing).take(MAX_ENTRIES)
    }
}
