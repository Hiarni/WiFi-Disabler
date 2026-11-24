package dev.hiarni.wifidisabler

import dev.hiarni.wifidisabler.core.Vendor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VendorTest {
    @Test fun `devices with an autostart switch`() {
        assertTrue(Vendor.hasAutostart("Xiaomi", "Redmi"))
        assertTrue(Vendor.hasAutostart("HUAWEI", "HUAWEI"))
        assertTrue(Vendor.hasAutostart("OPPO", "realme"))
        assertTrue(Vendor.hasAutostart("vivo", "iQOO"))
    }

    @Test fun `devices without an autostart switch`() {
        assertFalse(Vendor.hasAutostart("samsung", "samsung"))
        assertFalse(Vendor.hasAutostart("Google", "google"))
        assertFalse(Vendor.hasAutostart("motorola", "motorola"))
        assertFalse(Vendor.hasAutostart("Nothing", "Nothing"))
    }
}
