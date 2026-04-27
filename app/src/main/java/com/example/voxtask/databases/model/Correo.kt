package com.example.voxtask.databases.model

data class Correo(
    val id: String = "",
    val asunto: String = "(sin asunto)",
    val remitente: String = "",
    val fecha: String = "",
    val cuerpo: String = ""
)

