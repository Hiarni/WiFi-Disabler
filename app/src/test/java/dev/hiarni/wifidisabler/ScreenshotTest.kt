package dev.hiarni.wifidisabler

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import dev.hiarni.wifidisabler.core.GuardEvent
import dev.hiarni.wifidisabler.core.SetupState
import dev.hiarni.wifidisabler.data.Settings
import dev.hiarni.wifidisabler.ui.MainActions
import dev.hiarni.wifidisabler.ui.MainScreen
import dev.hiarni.wifidisabler.ui.UiState
import dev.hiarni.wifidisabler.ui.theme.WifiDisablerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Rendert die README-Screenshots auf der JVM (ohne Emulator).
 * Neu erzeugen: ./gradlew recordRoborazziDebug
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE)
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private val now = 1_795_500_000_000L

    private val configured = UiState(
        settings = Settings(
            enabled = true,
            blocked = setOf("Office-Guest", "Car Hotspot", "Hotel_Lobby"),
            events = listOf(
                GuardEvent(now - 25 * 60_000L, "Car Hotspot", true),
                GuardEvent(now - 26 * 3_600_000L, "Office-Guest", true),
            ),
        ),
        setup = SetupState(locationGranted = true, backgroundLocationGranted = true, locationServiceOn = true, batteryUnrestricted = true),
        currentSsid = "Corner Cafe",
        versionName = "1.0.0",
        loaded = true,
    )

    private val fresh = UiState(
        settings = Settings(),
        setup = SetupState(locationGranted = false, backgroundLocationGranted = false, locationServiceOn = true, batteryUnrestricted = false),
        versionName = "1.0.0",
        loaded = true,
    )

    private fun shoot(dir: String, name: String, state: UiState, dark: Boolean = false) {
        compose.setContent {
            WifiDisablerTheme(darkTheme = dark, dynamicColor = false) { MainScreen(state, MainActions()) }
        }
        compose.onRoot().captureRoboImage("../docs/$dir$name.png")
    }

    @Test fun main_de() = shoot("", "start", configured)

    @Test fun main_de_dark() = shoot("", "dunkel", configured, dark = true)

    @Test fun setup_de() = shoot("", "einrichtung", fresh)

    @Config(qualifiers = "en-$PHONE")
    @Test fun main_en() = shoot("en/", "start", configured)

    @Config(qualifiers = "en-$PHONE")
    @Test fun main_en_dark() = shoot("en/", "dark", configured, dark = true)

    @Config(qualifiers = "en-$PHONE")
    @Test fun setup_en() = shoot("en/", "setup", fresh)
}

private const val PHONE = "w411dp-h891dp-xxhdpi"
