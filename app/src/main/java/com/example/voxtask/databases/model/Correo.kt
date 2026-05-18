package com.example.voxtask.databases.model

data class Correo(
    val id: String = "",
    val asunto: String = "(sin asunto)",
    val remitente: String,           // nombre visible
    val emailRemitente: String?,     // ← añade esto
    val fecha: String = "",
    val cuerpo: String = ""
)

