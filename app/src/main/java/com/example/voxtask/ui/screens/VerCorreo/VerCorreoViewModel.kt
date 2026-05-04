package com.example.voxtask.ui.screens.VerCorreo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Correo
import com.example.voxtask.databases.network.N8nClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VerCorreoViewModel : ViewModel() {

    var correo by mutableStateOf<Correo?>(null)
        private set
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun obtenerTokenYCorreo(id: String) {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .get()
                    .await()
                val token = doc.getString("gmailAccessToken") ?: return@launch

                // Añade esto para ver la URL exacta
                android.util.Log.d("VerCorreo", "Llamando con id: $id")
                android.util.Log.d("VerCorreo", "URL: ${N8nClient.BASE_URL}webhook/correo-detalle/correo/$id")

                correo = N8nClient.api.obtenerCorreoPorId(id, token)
            } catch (e: Exception) {
                error = "Error al cargar el correo: ${e.message}"
                android.util.Log.e("VerCorreo", "Error: ${e.message}", e)
            } finally {
                cargando = false
            }
        }
    }

}