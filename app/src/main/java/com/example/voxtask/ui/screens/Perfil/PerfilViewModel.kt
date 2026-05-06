package com.example.voxtask.ui.screens.Perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var avatarUrl by mutableStateOf<String?>(null)
    var nombre by mutableStateOf("")
    var nombreUsuario by mutableStateOf("")
    var primerApellido by mutableStateOf("")
    var segundoApellido by mutableStateOf("")

    var modoEdicion by mutableStateOf(false)
    var cargando by mutableStateOf(false)
    var mensaje by mutableStateOf<String?>(null)

    init {
        cargarPerfil()
    }

    fun subirAvatar(uri: android.net.Uri) {
        val idUsuario = auth.currentUser?.uid ?: return
        val referencia = FirebaseStorage.getInstance()
            .reference.child("avatars/$idUsuario.jpg")

        cargando = true

        referencia.putFile(uri)
            .continueWithTask { tarea ->
                if (!tarea.isSuccessful) throw tarea.exception ?: Exception("Error al subir imagen")
                referencia.downloadUrl
            }
            .addOnSuccessListener { urlDescarga ->
                val url = urlDescarga.toString()
                avatarUrl = url
                firestore.collection("usuarios").document(idUsuario).update("avatar", url)
                mensaje = "✅ Avatar actualizado"
                cargando = false
            }
            .addOnFailureListener {
                mensaje = "❌ Error al subir avatar"
                cargando = false
            }
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            cargando = true
            try {
                val idUsuario = auth.currentUser?.uid ?: return@launch
                val documento = firestore.collection("usuarios").document(idUsuario).get().await()
                nombre = documento.getString("nombre") ?: ""
                nombreUsuario = documento.getString("nombre_usuario") ?: ""
                primerApellido = documento.getString("primer_apellido") ?: ""
                segundoApellido = documento.getString("segundo_apellido") ?: ""
                avatarUrl = documento.getString("avatar")
            } catch (e: Exception) {
                mensaje = "❌ Error al cargar el perfil"
            } finally {
                cargando = false
            }
        }
    }

    fun guardarPerfil() {
        viewModelScope.launch {
            cargando = true
            try {
                val idUsuario = auth.currentUser?.uid ?: return@launch
                firestore.collection("usuarios").document(idUsuario).update(
                    mapOf(
                        "nombre" to nombre,
                        "nombre_usuario" to nombreUsuario,
                        "primer_apellido" to primerApellido,
                        "segundo_apellido" to segundoApellido,
                        "avatar" to avatarUrl
                    )
                ).await()
                mensaje = "✅ Perfil guardado correctamente"
                modoEdicion = false
            } catch (e: Exception) {
                mensaje = "❌ Error al guardar el perfil"
            } finally {
                cargando = false
            }
        }
    }
}