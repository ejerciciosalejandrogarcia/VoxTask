package com.example.voxtask.databases.dao

import com.example.voxtask.databases.model.Usuario
/**
 * Interfaz que define las operaciones sobre los usuarios en el login y en el email
 */
interface UsuarioDao {
    suspend fun iniciarSesion(email: String, contrasena: String): Result<Usuario>
    suspend fun registrarUsuario(usuario: Usuario): Result<Usuario>
    suspend fun obtenerUsuario(uid: String): Result<Usuario>
    suspend fun obtenerUsuarioPorEmail(email: String): Result<Usuario>
    suspend fun correoEstaRegistrado(email: String): Boolean

}