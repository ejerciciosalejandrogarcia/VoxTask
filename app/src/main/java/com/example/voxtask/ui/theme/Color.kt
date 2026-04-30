package com.example.voxtask.ui.theme

import androidx.compose.ui.graphics.Color

// Colores existentes
val VerdePrimario = Color(0xFF2E7D32)
val VerdeClaro = Color(0xFF4CAF50)
val VerdeMenta = Color(0xFF81C784)
val MoradoBoton = Color(0xFF7C4DFF)
val MoradoSuave = Color(0xFFEDE7F6)
val FondoBlanco = Color(0xFFFAFAFA)
val TextoOscuro = Color(0xFF1B1B1B)
val TextoGris = Color(0xFF757575)

// Paleta de colores para el selector
val ColoresDisponibles = listOf(
 Color(0xFFF44336), // Rojo
 Color(0xFFE91E63), // Rosa
 Color(0xFF9C27B0), // Morado
 Color(0xFF2196F3), // Azul
 Color(0xFF00BCD4), // Cyan
 Color(0xFF4CAF50), // Verde
 Color(0xFFFFEB3B), // Amarillo
 Color(0xFFFF9800), // Naranja
 VerdePrimario,      // Verde primario
 Color(0xFF795548)  // Marrón
)

enum class ModoTema { CLARO, OSCURO }