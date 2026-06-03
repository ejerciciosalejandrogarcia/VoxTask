package com.example.voxtask.ui.screens.VerCorreo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.model.Correo
import com.example.voxtask.databases.network.N8nClient
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
/**
 * Define los posibles estados de la interfaz en la pantalla
 */
sealed class VerCorreoUiState {
    object Cargando : VerCorreoUiState()
    data class Exito(val correo: Correo) : VerCorreoUiState()
    data class Error(val mensaje: Int, val esErrorDeCarga: Boolean = false) : VerCorreoUiState()
}

class VerCorreoViewModel : ViewModel() {
    /** Variables */
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<VerCorreoUiState>(VerCorreoUiState.Cargando)
    val uiState: StateFlow<VerCorreoUiState> = _uiState
    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorFlow = _errorChannel.receiveAsFlow()
    private var correoId: String? = null
    /**
     * Permite validar la sesión del usuario, refrescar el token de acceso de Google
     * y lo sincroniza en Firestore para habilitar la lectura de correos
     */
    fun obtenerTokenYCorreo(id: String, contexto: Context) {
        correoId = id
        viewModelScope.launch {
            _uiState.value = VerCorreoUiState.Cargando

            val uid = auth.currentUser?.uid
            if (uid == null) {
                _errorChannel.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_sin_sesion))
                return@launch
            }

            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                val emailFirebase = auth.currentUser?.email ?: ""

                if (cuentaGoogle?.account == null || cuentaGoogle.email != emailFirebase) {
                    _uiState.value = VerCorreoUiState.Error(
                        mensaje = R.string.error_general,
                        esErrorDeCarga = false
                    )
                    return@launch
                }

                val accessToken = try {
                    withContext(Dispatchers.IO) {
                        val tokenActual = GoogleAuthUtil.getToken(
                            contexto,
                            cuentaGoogle.account!!,
                            "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                        )
                        GoogleAuthUtil.clearToken(contexto, tokenActual)
                        GoogleAuthUtil.getToken(
                            contexto,
                            cuentaGoogle.account!!,
                            "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("VerCorreo", "Error obteniendo token: ${e.message}")
                    _errorChannel.send(contexto.getString(R.string.error_general))
                    return@launch
                }

                firestore.collection("usuarios")
                    .document(uid)
                    .set(
                        mapOf("gmailAccessToken" to accessToken),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .await()

                cargarCorreo(id, accessToken, contexto)

            } catch (e: Exception) {
                android.util.Log.e("VerCorreo", "Exception general: ${e.message}")
                _errorChannel.send(contexto.getString(R.string.error_general))
            }
        }
    }

    /**
     * Permite realiza la petición a la API para obtener el cuerpo del correo
     */
    private suspend fun cargarCorreo(id: String, token: String, contexto: Context) {
        try {
            android.util.Log.d("VerCorreo", "Llamando con id: $id")
            val correo = N8nClient.api.obtenerCorreoPorId(id, token)
            _uiState.value = VerCorreoUiState.Exito(correo)
        } catch (e: Exception) {
            android.util.Log.e("VerCorreo", "Error cargando correo: ${e.message}")
            _errorChannel.send(contexto.getString(R.string.error_cargar_correo))
            _uiState.value = VerCorreoUiState.Error(
                mensaje = R.string.error_cargar_correo,
                esErrorDeCarga = true
            )
        }
    }

    /**
     * Permite intenta recuperar el flujo de obtención de datos
     */
    fun reintentar(contexto: Context) {
        correoId?.let { obtenerTokenYCorreo(it, contexto) }
    }
}