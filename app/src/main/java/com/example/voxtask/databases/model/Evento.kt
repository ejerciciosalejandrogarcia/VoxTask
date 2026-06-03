package com.example.voxtask.databases.model
/**
 * Modelo que representa un evento
 */
data class Evento(
    val id: String = "",
    val asunto: String = "",
    val dia: Int = 0,
    val mes: Int = 0,
    val anio: Int = 0
)