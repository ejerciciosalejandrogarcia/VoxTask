package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.UsuarioDao
import com.example.voxtask.databases.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository : UsuarioDao {

    private val auth = FirebaseAuth.getInstance()
    private  val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("usuarios")

    override suspend fun registrarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            // Crear cuenta en Firebase Auth
            val authResult = auth
                .createUserWithEmailAndPassword(usuario.correo_electronico, usuario.contrasenia)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            // Guardar datos
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
            val query = coleccion
                .whereEqualTo("nombre_usuario", nombreUsuario)
                .get()
                .await()
            val documento = query.documents.firstOrNull()
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val usuario = documento.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Error al obtener usuario"))
            val email = usuario.correo_electronico
            val authResult = auth
                .signInWithEmailAndPassword(email, contrasena)
                .await()
            if (authResult.user == null) {
                return Result.failure(Exception("Error en autenticación"))
            }
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerUsuarioPorEmail(email: String): Result<Usuario> {
        return try {
            val query = coleccion
                .whereEqualTo("correo_electronico", email)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val usuario = query.documents[0].toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Error al convertir usuario"))

            Result.success(usuario)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun correoEstaRegistrado(email: String): Boolean {
        return try {
            val result = auth.fetchSignInMethodsForEmail(email).await()
            !result.signInMethods.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
}