package com.example.voxtask.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voxtask.R

/**
 * Define las categorías de tamaño de pantalla para adaptar
 * la interfaz de usuario de forma responsiva
 */
enum class TamanioPantalla { COMPACTO, MEDIO, EXPANDIDO }

fun WindowWidthSizeClass.aTamanioPantalla() = when (this) {
    WindowWidthSizeClass.Compact -> TamanioPantalla.COMPACTO
    WindowWidthSizeClass.Medium  -> TamanioPantalla.MEDIO
    else                         -> TamanioPantalla.EXPANDIDO
}

/**
 * Convierte la clasificación de tamaño de pantalla del sistema
 * al formato personalizado de la aplicación
 */
data class Espaciado(
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp
)
/**
 * Permite crear una instancia de espaciado cargando los valores definidos en los
 * dimens.xml
 */
@Composable
fun espaciadoFromDimens() = Espaciado(
    xs  = dimensionResource(R.dimen.espaciado_xs),
    s   = dimensionResource(R.dimen.espaciado_s),
    m   = dimensionResource(R.dimen.espaciado_m),
    l   = dimensionResource(R.dimen.espaciado_l),
    xl  = dimensionResource(R.dimen.espaciado_xl),
    xxl = dimensionResource(R.dimen.espaciado_xxl),
)
/** Variables */
val LocalEspaciado       = compositionLocalOf { Espaciado(4.dp, 8.dp, 12.dp, 16.dp, 24.dp, 32.dp) }
val LocalTamanioPantalla = compositionLocalOf { TamanioPantalla.COMPACTO }

// Define el estilo y distribución según el dispositivo
val TamanioPantalla.textoBody: TextUnit get() = when (this) {
    TamanioPantalla.COMPACTO  -> 14.sp
    TamanioPantalla.MEDIO     -> 16.sp
    TamanioPantalla.EXPANDIDO -> 18.sp
}

val TamanioPantalla.textoTitulo: TextUnit get() = when (this) {
    TamanioPantalla.COMPACTO  -> 20.sp
    TamanioPantalla.MEDIO     -> 24.sp
    TamanioPantalla.EXPANDIDO -> 28.sp
}

val TamanioPantalla.columnas: Int get() = when (this) {
    TamanioPantalla.COMPACTO  -> 1
    TamanioPantalla.MEDIO     -> 2
    TamanioPantalla.EXPANDIDO -> 3
}

val TamanioPantalla.anchoMaximoContenido: Dp get() = when (this) {
    TamanioPantalla.COMPACTO  -> Dp.Unspecified
    TamanioPantalla.MEDIO     -> 600.dp
    TamanioPantalla.EXPANDIDO -> 840.dp
}