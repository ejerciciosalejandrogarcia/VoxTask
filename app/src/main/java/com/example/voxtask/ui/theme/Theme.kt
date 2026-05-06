package com.example.voxtask.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.compositionLocalOf

class ThemeManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("tema_ajustes", Context.MODE_PRIVATE)

    // Color para modo claro — por defecto VerdePrimario
    private val _colorClaro = MutableStateFlow(
        Color(prefs.getInt("color_claro", VerdePrimario.toArgb()))
    )
    val colorClaro: StateFlow<Color> = _colorClaro.asStateFlow()

    // Color para modo oscuro — por defecto VerdePrimario
    private val _colorOscuro = MutableStateFlow(
        Color(prefs.getInt("color_oscuro", VerdePrimario.toArgb()))
    )
    val colorOscuro: StateFlow<Color> = _colorOscuro.asStateFlow()

    fun setColorClaro(color: Color) {
        _colorClaro.value = color
        prefs.edit().putInt("color_claro", color.toArgb()).apply()
    }

    fun setColorOscuro(color: Color) {
        _colorOscuro.value = color
        prefs.edit().putInt("color_oscuro", color.toArgb()).apply()
    }

    fun resetearColores() {
        val colorDefault = VerdePrimario

        _colorClaro.value = colorDefault
        _colorOscuro.value = colorDefault

        prefs.edit()
            .putInt("color_claro", colorDefault.toArgb())
            .putInt("color_oscuro", colorDefault.toArgb())
            .apply()
    }
}

@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember { ThemeManager(context) }
}

val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager") }

@Composable
fun VoxTaskTheme(
    themeManager: ThemeManager = rememberThemeManager(),
    content: @Composable () -> Unit
) {
    val colorClaro by themeManager.colorClaro.collectAsState()
    val colorOscuro by themeManager.colorOscuro.collectAsState()

    // Detecta automáticamente el modo del dispositivo
    val esModoOscuro = isSystemInDarkTheme()

    val colorScheme = if (esModoOscuro) {
        darkColorScheme(
            primary = colorOscuro,
            onPrimary = Color.White,
            primaryContainer = colorOscuro.copy(alpha = 0.2f),
            onPrimaryContainer = Color.White,
            secondary = colorOscuro.copy(alpha = 0.7f),
            onSecondary = Color.White,
            background = Color(0xFF121212),
            onBackground = Color.White,
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = colorClaro,
            onPrimary = Color.White,
            primaryContainer = colorClaro.copy(alpha = 0.1f),
            onPrimaryContainer = colorClaro,
            secondary = colorClaro.copy(alpha = 0.7f),
            onSecondary = Color.White,
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF1B1B1B),
            surface = Color.White,
            onSurface = Color(0xFF1B1B1B)
        )
    }

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}