package dev.hiarni.wifidisabler.core

/** Helpers for the raw SSID strings Android hands out. Pure Kotlin, unit-tested. */
object Ssid {
    /** Placeholder Android returns when the SSID is hidden from the app (e.g. no location access). */
    const val UNKNOWN = "<unknown ssid>"

    /**
     * Converts [android.net.wifi.WifiInfo.getSSID] output into a plain network name.
     * UTF-8 names come wrapped in quotes; non-UTF-8 names come as raw hex (kept as-is).
     * Returns `null` when no usable name is available.
     */
    fun normalize(raw: String?): String? {
        if (raw.isNullOrEmpty() || raw == UNKNOWN) return null
        val name = if (raw.length >= 2 && raw.first() == '"' && raw.last() == '"') {
            raw.substring(1, raw.length - 1)
        } else {
            raw
        }
        return name.ifEmpty { null }
    }

    /** Cleans up a name typed by the user. Returns `null` if nothing usable is left. */
    fun fromUserInput(input: String): String? {
        val trimmed = input.trim()
        return normalize(trimmed)?.takeIf { it.length <= MAX_LENGTH_CHARS }
    }

    /** SSIDs are case-sensitive, so matching is exact. */
    fun isBlocked(ssid: String?, blocked: Collection<String>): Boolean =
        ssid != null && ssid in blocked

    /** 32 bytes per IEEE 802.11; generous upper bound for multi-byte characters. */
    private const val MAX_LENGTH_CHARS = 32
}
