package com.example.voxtask.ui.screens.Perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilViewModel : ViewModel() {

    //Variables
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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

    // Carga los datos del usuario desde Firebase
    private fun cargarPerfil() {
        viewModelScope.launch {
            cargando = true
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val doc = firestore.collection("usuarios").document(uid).get().await()
                nombre = doc.getString("nombre") ?: ""
                nombreUsuario = doc.getString("nombre_usuario") ?: ""
                primerApellido = doc.getString("primer_apellido") ?: ""
                segundoApellido = doc.getString("segundo_apellido") ?: ""
            } catch (e: Exception) {
                mensaje = "Error al cargar el perfil"
            } finally {
                cargando = false
            }
        }
    }

    // Guarda los cambios en Firebase
    fun guardarPerfil() {
        viewModelScope.launch {
            cargando = true
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                firestore.collection("usuarios").document(uid)
                    .update(
                        mapOf(
                            "nombre" to nombre,
                            "nombre_usuario" to nombreUsuario,
                            "primer_apellido" to primerApellido,
                            "segundo_apellido" to segundoApellido
                        )
                    ).await()
                mensaje = "Perfil guardado correctamente"
                modoEdicion = false
            } catch (e: Exception) {
                mensaje = "Error al guardar el perfil"
            } finally {
                cargando = false
            }
        }
    }
}