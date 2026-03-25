package com.example.voxtask.databases.dao

import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.databases.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioDao : UsuarioRepository {

    private val auth = FirebaseAuth.getInstance()
    private  val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("usuarios")

    override suspend fun registrarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            // 1. Crear cuenta en Firebase Auth
            val authResult = auth
                .createUserWithEmailAndPassword(usuario.correo_electronico, usuario.contrasenia)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            // 2. Guardar datos extra en Firestore (sin contraseña)
            val usuarioConId = usuario.copy(id = uid, contrasenia = "")
            coleccion.document(uid).set(usuarioConId).await()

            Result.success(usuarioConId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerUsuario(uid: String): Result<Usuario> {
        return try {
            val doc = coleccion.document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesion(
        nombreUsuario: String,
        contrasena: String
    ): Result<Usuario> {
        return try {

            // 🔍 1. Buscar usuario por nombre_usuario
            val query = coleccion
                .whereEqualTo("nombre_usuario", nombreUsuario)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val usuario = query.documents[0].toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Error al obtener usuario"))

            val email = usuario.correo_electronico

            // 🔐 2. Login con email real
            val authResult = auth
                .signInWithEmailAndPassword(email, contrasena)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener UID"))

            Result.success(usuario)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
