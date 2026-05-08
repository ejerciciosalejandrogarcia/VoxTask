package com.example.voxtask.ui.screens.Cambiar_contrasenia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.network.N8nClient
import com.example.voxtask.databases.network.RecuperarContraseniaRequest
import com.google.firebase.auth.ActionCodeSettings
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

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://voxtask-de969.web.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.voxtask", true, null)
            .build()

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(cargando = true, mensajeError = "")
            try {
                auth.sendPasswordResetEmail(email, actionCodeSettings).await()

                /*
                N8nClient.api.enviarCorreoRecuperacion(
                    RecuperarContraseniaRequest(email = email)
                )*/

                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    correoEnviado = true
                )
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    mensajeError = e.message ?: "Error. Inténtalo de nuevo"
                )
            }
        }
    }

    fun guardarNuevaContrasena(oobCode: String) {
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
                // ✅ Cambia la contraseña con el oobCode de Firebase
                auth.confirmPasswordReset(oobCode, nueva).await()

                _estadoNueva.value = _estadoNueva.value.copy(
                    cargando = false,
                    cambioExitoso = true
                )
            } catch (e: Exception) {
                _estadoNueva.value = _estadoNueva.value.copy(
                    cargando = false,
                    mensajeError = e.message ?: "Error al cambiar la contraseña"
                )
            }
        }
    }

    fun reiniciar() {
        _estadoUi.value = CambiarContrasenaUiState()
    }

}