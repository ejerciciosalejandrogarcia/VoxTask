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

    /** Variables */
    private val prefs = context.getSharedPreferences("tema_ajustes", Context.MODE_PRIVATE)

    private val _colorClaro = MutableStateFlow(
        Color(prefs.getInt("color_claro", VerdePrimario.toArgb()))
    )
    val colorClaro: StateFlow<Color> = _colorClaro.asStateFlow()

    private val _colorOscuro = MutableStateFlow(
        Color(prefs.getInt("color_oscuro", VerdePrimario.toArgb()))
    )
    val colorOscuro: StateFlow<Color> = _colorOscuro.asStateFlow()

    /**
     * Permite guardar el color claro seleccionado por el usuario y que persista despues de cerrar la aplicacion
     */
    fun setColorClaro(color: Color) {
        _colorClaro.value = color
        prefs.edit().putInt("color_claro", color.toArgb()).apply()
    }
    /**
     * Permite guardar el color oscuro seleccionado por el usuario y que persista despues de cerrar la aplicacion
     */
    fun setColorOscuro(color: Color) {
        _colorOscuro.value = color
        prefs.edit().putInt("color_oscuro", color.toArgb()).apply()
    }

    /**
     * Permite resetear los colores personalizados de la aplicación a sus
     * valores predeterminados (VerdePrimario)
     */
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

/**
 * Permite crear y guardar una única instancia del gestor de temas
 */
@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember { ThemeManager(context) }
}

/**
 * Permite que cualquier componente de la interfaz cambiar el tema sin pasar el objeto por parámetro
 */
val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager") }

/**
 * Permite configurar los colores de la app y los adapta
 * automáticamente al modo claro u oscuro del dispositivo
 */
@Composable
fun VoxTaskTheme(
    themeManager: ThemeManager = rememberThemeManager(),
    content: @Composable () -> Unit
) {
    val colorClaro by themeManager.colorClaro.collectAsState()
    val colorOscuro by themeManager.colorOscuro.collectAsState()

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