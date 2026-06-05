package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.UsuarioDao
import com.example.voxtask.databases.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
/**
 * Repositorio encargado de las operaciones para los usuarios
 */
class UsuarioRepository : UsuarioDao {

    /** Variables */
    private val autenticacion = FirebaseAuth.getInstance()
    private  val bd = FirebaseFirestore.getInstance()
    private val coleccion = bd.collection("usuarios")

    /**
     * Registra un nuevo usuario en Firebase Authentication y persiste su información en Firestore
     */
    override suspend fun registrarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val resultadoAutenticacion = autenticacion
                .createUserWithEmailAndPassword(usuario.correo_electronico, usuario.contrasenia)
                .await()
            val uid = resultadoAutenticacion.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el UID"))
            val usuarioConId = usuario.copy(id = uid, contrasenia = "")
            coleccion.document(uid).set(usuarioConId).await()
            Result.success(usuarioConId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los datos de un usuario desde Firestore mediante su identificador
     */
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

    /**
     * Permite a los usuarios a iniciar sesion mediante su nombre de usuario y contraseña
     */
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
            val resultadoAutenticacion = autenticacion
                .signInWithEmailAndPassword(email, contrasena)
                .await()
            if (resultadoAutenticacion.user == null) {
                return Result.failure(Exception("Error en autenticación"))
            }
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Busca el usuario mediante su dirección de correo electrónico en Firestore
     */
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

    /**
     * Verifica si un correo electrónico ya está registrado en Firebase Authentication.
     */
    override suspend fun correoEstaRegistrado(email: String): Boolean {
        return try {
            val resultado = autenticacion.fetchSignInMethodsForEmail(email).await()
            !resultado.signInMethods.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
}