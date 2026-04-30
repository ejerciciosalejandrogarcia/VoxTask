package com.example.voxtask.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.room.util.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Colores predefinidos
val ColoresDisponibles = listOf(
    Color(0xFFF44336), // Rojo
    Color(0xFFE91E63), // Rosa
    Color(0xFF9C27B0), // Morado
    Color(0xFF2196F3), // Azul
    Color(0xFF00BCD4), // Cyan
    Color(0xFF4CAF50), // Verde
    Color(0xFFFFEB3B), // Amarillo
    Color(0xFFFF9800), // Naranja
    Color(0xFF2E7D32), // Verde oscuro
    Color(0xFF795548)  // Marrón
)

enum class ModoTema { CLARO, OSCURO }

// ThemeManager para manejar el estado del tema
class ThemeManager {
    private val _modoTema = MutableStateFlow(ModoTema.CLARO)
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    private val _colorPrincipal = MutableStateFlow(ColoresDisponibles[5]) // Verde por defecto
    val colorPrincipal: StateFlow<Color> = _colorPrincipal.asStateFlow()

    fun setModoTema(modo: ModoTema) {
        _modoTema.value = modo
    }

    fun setColorPrincipal(color: Color) {
        _colorPrincipal.value = color
    }

    @Composable
    fun getColorScheme(): androidx.compose.material3.ColorScheme {
        val color = _colorPrincipal.value

        return if (_modoTema.value == ModoTema.OSCURO) {
            darkColorScheme(
                primary = color,
                onPrimary = Color.White,
                primaryContainer = color.copy(alpha = 0.2f),
                onPrimaryContainer = Color.White,
                secondary = color.copy(alpha = 0.7f),
                onSecondary = Color.White,
                background = Color(0xFF121212),
                onBackground = Color.White,
                surface = Color(0xFF1E1E1E),
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = color,
                onPrimary = Color.White,
                primaryContainer = color.copy(alpha = 0.1f),
                onPrimaryContainer = color,
                secondary = color.copy(alpha = 0.7f),
                onSecondary = Color.White,
                background = Color(0xFFFAFAFA),
                onBackground = Color(0xFF1B1B1B),
                surface = Color.White,
                onSurface = Color(0xFF1B1B1B)
            )
        }
    }
}

@Composable
fun rememberThemeManager(): ThemeManager = remember { ThemeManager() }

val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager") }

@Composable
fun VoxTaskTheme(
    themeManager: ThemeManager = rememberThemeManager(),
    content: @Composable () -> Unit
) {
    val colorScheme = themeManager.getColorScheme()

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}