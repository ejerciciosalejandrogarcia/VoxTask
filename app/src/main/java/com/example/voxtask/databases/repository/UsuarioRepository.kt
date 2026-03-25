package com.example.voxtask.databases.repository


import com.example.voxtask.databases.model.Usuario

interface UsuarioRepository {
    suspend fun iniciarSesion(email: String, contrasena: String): Result<Usuario>
    suspend fun registrarUsuario(usuario: Usuario): Result<Usuario>
    suspend fun obtenerUsuario(uid: String): Result<Usuario>
}