package com.example.voxtask.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TamanioPantalla { COMPACTO, MEDIO, EXPANDIDO }

fun WindowWidthSizeClass.aTamanioPantalla() = when (this) {
    WindowWidthSizeClass.Compact -> TamanioPantalla.COMPACTO
    WindowWidthSizeClass.Medium  -> TamanioPantalla.MEDIO
    else                         -> TamanioPantalla.EXPANDIDO
}

data class Espaciado(
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp
)

val EspaciadoCompacto  = Espaciado(xs = 4.dp,  s = 8.dp,  m = 12.dp, l = 16.dp, xl = 24.dp)
val EspaciadoMedio     = Espaciado(xs = 6.dp,  s = 12.dp, m = 16.dp, l = 24.dp, xl = 32.dp)
val EspaciadoExpandido = Espaciado(xs = 8.dp,  s = 16.dp, m = 24.dp, l = 32.dp, xl = 48.dp)

val LocalEspaciado        = compositionLocalOf { EspaciadoCompacto }
val LocalTamanioPantalla  = compositionLocalOf { TamanioPantalla.COMPACTO }

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