package dev.hiarni.wifidisabler.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Brand accent used for the "cut" in the logo. */
val SignalOrange = Color(0xFFFF8A3D)

private val Light: ColorScheme = lightColorScheme(
    primary = Color(0xFF0E6670), onPrimary = Color.White,
    primaryContainer = Color(0xFFA7EEF7), onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6267), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE7EC), onSecondaryContainer = Color(0xFF051F23),
    tertiary = Color(0xFF9A4600), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBC9), onTertiaryContainer = Color(0xFF331200),
    background = Color(0xFFF5FAFB), onBackground = Color(0xFF171D1E),
    surface = Color(0xFFF5FAFB), onSurface = Color(0xFF171D1E),
    surfaceVariant = Color(0xFFDBE4E6), onSurfaceVariant = Color(0xFF3F484A),
    outline = Color(0xFF6F797A), outlineVariant = Color(0xFFBFC8CA),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFEFF5F6),
    surfaceContainer = Color(0xFFE9EFF0), surfaceContainerHigh = Color(0xFFE3E9EA),
    surfaceContainerHighest = Color(0xFFDEE3E5),
)

private val Dark: ColorScheme = darkColorScheme(
    primary = Color(0xFF82D3DE), onPrimary = Color(0xFF00363C),
    primaryContainer = Color(0xFF004F57), onPrimaryContainer = Color(0xFFA7EEF7),
    secondary = Color(0xFFB1CBD0), onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF334B4F), onSecondaryContainer = Color(0xFFCDE7EC),
    tertiary = Color(0xFFFFB68D), onTertiary = Color(0xFF532200),
    tertiaryContainer = Color(0xFF763300), onTertiaryContainer = Color(0xFFFFDBC9),
    background = Color(0xFF0E1415), onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF0E1415), onSurface = Color(0xFFDEE3E5),
    surfaceVariant = Color(0xFF3F484A), onSurfaceVariant = Color(0xFFBFC8CA),
    outline = Color(0xFF899294), outlineVariant = Color(0xFF3F484A),
    surfaceContainerLowest = Color(0xFF090F10), surfaceContainerLow = Color(0xFF171D1E),
    surfaceContainer = Color(0xFF1B2122), surfaceContainerHigh = Color(0xFF252B2C),
    surfaceContainerHighest = Color(0xFF303637),
)

@Composable
fun WifiDisablerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
