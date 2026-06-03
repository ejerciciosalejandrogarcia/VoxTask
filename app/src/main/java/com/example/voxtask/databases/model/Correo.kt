package com.example.voxtask.databases.model

/**
 * Modelo que representa un correo electrónico
 */
data class Correo(
    val id: String = "",
    val asunto: String = "(sin asunto)",
    val remitente: String,
    val emailRemitente: String?,
    val fecha: String = "",
    val cuerpo: String = ""
)

