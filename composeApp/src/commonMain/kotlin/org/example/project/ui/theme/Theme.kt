package org.example.project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary, // Color de tus botones
    onPrimary = BackgroundWhite, // Color del texto dentro de los botones
    background = BackgroundWhite, // Fondo de la app
    onBackground = TextBlack, // Color del texto
    surface = BackgroundWhite, // Fondo de las cards o barras
    onSurface = TextBlack, // Color del texto en cards
    onSurfaceVariant = TextSecondary, // Textos secundarios
    outline = GrayBorder, // Bordes de los inputs
    error = Color(0xFFBA1A1A), // Mensajes de error
)

@Composable
fun ProjectManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}