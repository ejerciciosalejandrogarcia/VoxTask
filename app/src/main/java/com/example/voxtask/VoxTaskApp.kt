package com.example.voxtask

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

// Pantallas de la app
enum class VoxTaskScreen {
    Inicio_sesion
}

@Composable
fun VoxTaskApp(
    windowSize: WindowWidthSizeClass,

){
    val navController = rememberNavController()

}