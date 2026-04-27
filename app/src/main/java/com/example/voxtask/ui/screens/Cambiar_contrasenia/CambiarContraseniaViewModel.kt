package com.example.voxtask.ui.screens.Cambiar_contrasenia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CambiarContrasenaUiState(
    val email: String = "",
    val cargando: Boolean = false,
    val correoEnviado: Boolean = false,
    val mensajeError: String = ""
)

data class NuevaContraseniaUiState(
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val cambioExitoso: Boolean = false,
    val mensajeError: String = ""
)

class CambiarContraseniaViewModel : ViewModel() {
/*
    private val auth = FirebaseAuth.getInstance()

    private val _estadoUi = MutableStateFlow(CambiarContrasenaUiState())
    val estadoUi: StateFlow<CambiarContrasenaUiState> = _estadoUi.asStateFlow()

    private val _estadoNueva = MutableStateFlow(NuevaContraseniaUiState())
    val estadoNueva: StateFlow<NuevaContraseniaUiState> = _estadoNueva.asStateFlow()

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun alCambiarEmail(nuevoEmail: String) {
        _estadoUi.value = _estadoUi.value.copy(email = nuevoEmail, mensajeError = "")
    }

    fun alCambiarNuevaContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(nuevaContrasena = valor, mensajeError = "")
    }

    fun alCambiarConfirmarContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(confirmarContrasena = valor, mensajeError = "")
    }

    // ✅ FUNCIÓN QUE PEDISTE
    private suspend fun correoEstaRegistrado(email: String): Boolean {
        return try {
            val result = auth.fetchSignInMethodsForEmail(email).await()
            !result.signInMethods.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun enviarCorreoRecuperacion() {
        val email = _estadoUi.value.email.trim()

        if (email.isEmpty()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = "Introduce tu correo electrónico")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = "El formato del correo no es válido")
            return
        }

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(cargando = true, mensajeError = "")

            try {
                auth.sendPasswordResetEmail(email).await()

                val json = JSONObject().apply { put("email", email) }
                val body = json.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://192.168.1.40:5678/webhook/recuperar-contrasenia")
                    .post(body)
                    .build()

                cliente.newCall(request).execute()

                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    correoEnviado = true
                )

            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    mensajeError = "Error de conexión. Inténtalo de nuevo"
                )
            }
        }
    }

    fun guardarNuevaContrasena() {
        val nueva = _estadoNueva.value.nuevaContrasena
        val confirmar = _estadoNueva.value.confirmarContrasena

        if (nueva.length < 6) {
            _estadoNueva.value = _estadoNueva.value.copy(
                mensajeError = "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }

        if (nueva != confirmar) {
            _estadoNueva.value = _estadoNueva.value.copy(
                mensajeError = "Las contraseñas no coinciden"
            )
            return
        }

        viewModelScope.launch {
            _estadoNueva.value = _estadoNueva.value.copy(cargando = true, mensajeError = "")

            try {
                auth.currentUser?.updatePassword(nueva)?.await()

                _estadoNueva.value = _estadoNueva.value.copy(
                    cargando = false,
                    cambioExitoso = true
                )

            } catch (e: Exception) {
                _estadoNueva.value = _estadoNueva.value.copy(
                    cargando = false,
                    mensajeError = "Error al cambiar la contraseña. Vuelve a iniciar sesión e inténtalo de nuevo"
                )
            }
        }
    }

    fun reiniciar() {
        _estadoUi.value = CambiarContrasenaUiState()
    }
    */
}