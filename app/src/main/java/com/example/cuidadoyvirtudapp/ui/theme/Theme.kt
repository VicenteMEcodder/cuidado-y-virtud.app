package com.example.cuidadoyvirtudapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema claro fijo y de alto contraste (accesibilidad); sin color dinámico para mantener la identidad de marca.
private val EsquemaClaro = lightColorScheme(
    primary = Marino, onPrimary = Color.White,
    secondary = AzulMedio, onSecondary = Color.White,
    tertiary = Naranja, onTertiary = Color.White,
    background = Fondo, onBackground = Marino,
    surface = Color.White, onSurface = Marino,
    surfaceVariant = AzulPalido, onSurfaceVariant = Marino,
    error = Rojo,
)

@Composable
fun CuidadoYVirtudAPPTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaClaro, typography = Typography, content = content)
}
