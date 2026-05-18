package com.example.voxtask.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ProveedorAdaptativo(
    tamanioPantalla: WindowWidthSizeClass,
    contenido: @Composable () -> Unit
) {
    val tamano = tamanioPantalla.aTamanioPantalla()
    val espaciado = espaciadoFromDimens() // ← lee del dimens.xml correcto automáticamente

    CompositionLocalProvider(
        LocalTamanioPantalla provides tamano,
        LocalEspaciado provides espaciado
    ) {
        contenido()
    }
}