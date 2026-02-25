package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema claro
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BackgroundWhite,
    background = BackgroundWhite,
    onBackground = TextBlack,
    surface = BackgroundWhite,
    onSurface = TextBlack,
    onSurfaceVariant = TextSecondary,
    outline = GrayBorder,
    error = BackgroundError,
    onError = Color.White
)

// Tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFA0A0A0),
    outline = Color(0xFF333333),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun ProjectManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}