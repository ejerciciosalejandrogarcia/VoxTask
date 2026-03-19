package com.example.voxtask.databases.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Usuario(
    val id: String = "",
    val nombre_usuario: String,
    val nombre: String,
    val primer_apellido: String,
    val segundo_apellido: String,
    val fecha_nacimiento: String,
    val correo_electronico: String,
    val contrasenia: String,
    val verificado: Boolean = false,
    val fecha_creacion: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)
