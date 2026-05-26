package com.example.voxtask.ui.screens.Cambiar_contrasenia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CambiarContrasenaUiState(
    val email: String = "",
    val cargando: Boolean = false,
    val correoEnviado: Boolean = false,
    val mensajeError: Int = 0
)

data class NuevaContraseniaUiState(
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val cambioExitoso: Boolean = false,
    val mensajeError: Int? = null
)

class CambiarContraseniaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _estadoUi = MutableStateFlow(CambiarContrasenaUiState())
    val estadoUi: StateFlow<CambiarContrasenaUiState> = _estadoUi.asStateFlow()

    private val _estadoNueva = MutableStateFlow(NuevaContraseniaUiState())
    val estadoNueva: StateFlow<NuevaContraseniaUiState> = _estadoNueva.asStateFlow()

    fun alCambiarEmail(nuevoEmail: String) {
        _estadoUi.value = _estadoUi.value.copy(email = nuevoEmail, mensajeError = 0)
    }

    fun alCambiarNuevaContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(nuevaContrasena = valor, mensajeError = null)
    }

    fun alCambiarConfirmarContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(confirmarContrasena = valor, mensajeError = null)
    }

    fun enviarCorreoRecuperacion() {
        val email = _estadoUi.value.email.trim()

        if (email.isEmpty()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_email_vacio)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_email_invalido)
            return
        }

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://voxtask-de969.web.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.voxtask", true, null)
            .build()

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(cargando = true, mensajeError = 0)
            try {
                auth.sendPasswordResetEmail(email, actionCodeSettings).await()
                _estadoUi.value = _estadoUi.value.copy(cargando = false, correoEnviado = true)
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    mensajeError = R.string.error_envio_correo
                )
            }
        }
    }

    fun guardarNuevaContrasena(oobCode: String) {
        val nueva = _estadoNueva.value.nuevaContrasena
        val confirmar = _estadoNueva.value.confirmarContrasena
        val regexContrasenia = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$")

        when {
            nueva.isBlank() || confirmar.isBlank() -> {
                _estadoNueva.value = _estadoNueva.value.copy(mensajeError = R.string.error_campos_vacios)
                return
            }
            !regexContrasenia.matches(nueva) -> {
                _estadoNueva.value = _estadoNueva.value.copy(mensajeError = R.string.error_contrasena_debil)
                return
            }
            nueva != confirmar -> {
                _estadoNueva.value = _estadoNueva.value.copy(mensajeError = R.string.error_contrasenas_no_coinciden)
                return
            }
            else -> {
                viewModelScope.launch {
                    _estadoNueva.value = _estadoNueva.value.copy(cargando = true, mensajeError = null)
                    try {
                        auth.confirmPasswordReset(oobCode, nueva).await()
                        _estadoNueva.value = _estadoNueva.value.copy(cargando = false, cambioExitoso = true)
                    } catch (e: Exception) {
                        _estadoNueva.value = _estadoNueva.value.copy(
                            cargando = false,
                            mensajeError = R.string.error_cambio_contrasena
                        )
                    }
                }
            }
        }
    }

    fun reiniciar() {
        _estadoUi.value = CambiarContrasenaUiState()
    }

    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = 0)
    }

    fun limpiarErrorNueva() {
        _estadoNueva.value = _estadoNueva.value.copy(mensajeError = null)
    }
}