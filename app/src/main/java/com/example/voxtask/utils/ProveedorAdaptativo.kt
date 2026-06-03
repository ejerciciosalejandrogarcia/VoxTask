package com.example.voxtask.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ProveedorAdaptativo(
    tamanioPantalla: WindowWidthSizeClass,
    contenido: @Composable () -> Unit
) {
    /** Variables */
    val tamano = tamanioPantalla.aTamanioPantalla()
    val espaciado = espaciadoFromDimens()

    /**
     * Provee los valores globales de diseño (tamaño de pantalla y espaciado)
     * a todo el contenido de la aplicación
     */
    CompositionLocalProvider(
        LocalTamanioPantalla provides tamano,
        LocalEspaciado provides espaciado
    ) {
        contenido()
    }
}