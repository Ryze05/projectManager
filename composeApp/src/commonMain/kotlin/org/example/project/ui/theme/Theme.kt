package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- 1. TU TEMA CLARO ACTUAL ---
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

// --- 2. EL NUEVO TEMA OSCURO ---
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary, // Mantenemos tu azul principal
    onPrimary = Color.White,
    background = Color(0xFF121212), // Un gris casi negro para el fondo
    onBackground = Color(0xFFE0E0E0), // Texto claro
    surface = Color(0xFF1E1E1E), // Un gris un poco más claro para las tarjetas
    onSurface = Color(0xFFE0E0E0), // Texto claro en las tarjetas
    onSurfaceVariant = Color(0xFFA0A0A0), // Textos secundarios más tenues
    outline = Color(0xFF333333), // Bordes oscuros
    error = Color(0xFFCF6679), // Rojo adaptado para modo oscuro
    onError = Color.Black
)

@Composable
fun ProjectManagerTheme(
    // --- 3. EL PARÁMETRO QUE AVISA SI ESTÁ ENCENDIDO O APAGADO ---
    // (Por defecto lee la configuración del móvil, pero tu botón lo sobreescribirá)
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Comprobamos qué paleta usar
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