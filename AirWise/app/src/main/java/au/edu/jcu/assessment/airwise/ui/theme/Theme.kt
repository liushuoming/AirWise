package au.edu.jcu.assessment.airwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6F7F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDEBEF),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = Color(0xFF4A6267),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE7EC),
    onSecondaryContainer = Color(0xFF051F23),
    background = Color(0xFFF6FAFA),
    surface = Color(0xFFF6FAFA)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF85D2E0),
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004F5B),
    onPrimaryContainer = Color(0xFFCDEBEF),
    secondary = Color(0xFFB1CBD0),
    onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF334B4F),
    onSecondaryContainer = Color(0xFFCDE7EC)
)

/**
 * App theme. A fixed palette (not dynamic colour) keeps contrast predictable,
 * which matters for an app that explains colour-coded health categories.
 */
@Composable
fun AirWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
