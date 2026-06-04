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

/**
 * Representa el estado de la UI para la pantalla de la recuperación de contraseña
 */
data class CambiarContrasenaUiState(
    val correo: String = "",
    val cargando: Boolean = false,
    val correoEnviado: Boolean = false,
    val mensajeError: Int = 0
)

/**
 * Representa el estado de la UI para la pantalla de la nueva contraseña
 */
data class NuevaContraseniaUiState(
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val cambioExitoso: Boolean = false,
    val mensajeError: Int? = null
)

class CambiarContraseniaViewModel : ViewModel() {

    /** Variables */
    private val autenticacion = FirebaseAuth.getInstance()
    private val _estadoUi = MutableStateFlow(CambiarContrasenaUiState())
    val estadoUi: StateFlow<CambiarContrasenaUiState> = _estadoUi.asStateFlow()
    private val _estadoNueva = MutableStateFlow(NuevaContraseniaUiState())
    val estadoNueva: StateFlow<NuevaContraseniaUiState> = _estadoNueva.asStateFlow()

    /** Actualiza el correo electrónico en el estado y limpia cualquier mensaje de error */
    fun alCambiarCorreo(nuevoCorreo: String) {
        _estadoUi.value = _estadoUi.value.copy(correo = nuevoCorreo, mensajeError = 0)
    }
    /** Actualiza la nueva contraseña en el estado y limpia cualquier mensaje de error */
    fun alCambiarNuevaContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(nuevaContrasena = valor, mensajeError = null)
    }
    /** Actualiza la confirmacion de la nueva contraseña en el estado y limpia cualquier mensaje de error */
    fun alCambiarConfirmarContrasena(valor: String) {
        _estadoNueva.value = _estadoNueva.value.copy(confirmarContrasena = valor, mensajeError = null)
    }

    /**
     * Permite envíar un enlace de recuperación de contraseña al correo electrónico del usuario y redirecciona al usuario a la pantalla 'Nueva Contraseña'
     */
    fun enviarCorreoRecuperacion() {
        val correo = _estadoUi.value.correo.trim()

        if (correo.isEmpty()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_email_vacio)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_email_invalido)
            return
        }

        val configuracionAccion = ActionCodeSettings.newBuilder()
            .setUrl("https://voxtask-de969.web.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.voxtask", true, null)
            .build()

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(cargando = true, mensajeError = 0)
            try {
                autenticacion.sendPasswordResetEmail(correo, configuracionAccion).await()
                _estadoUi.value = _estadoUi.value.copy(cargando = false, correoEnviado = true)
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    cargando = false,
                    mensajeError = R.string.error_envio_correo
                )
            }
        }
    }

    /**
     * Permite validar la nueva contraseña y la guarda en Firebase
     */
    fun guardarNuevaContrasena(codigoOob: String) {
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
                        autenticacion.confirmPasswordReset(codigoOob, nueva).await()
                        _estadoNueva.value = _estadoNueva.value.copy(cargando = false, cambioExitoso = true)
                    } catch (e: Exception) {
                        _estadoNueva.value = _estadoNueva.value.copy(
                            cargando = false,
                            mensajeError = R.string.txt_error+R.string.error_cambio_contrasena
                        )
                    }
                }
            }
        }
    }

    /**
     * Permite reiniciar el estado de la UI de la pantalla 'Cambiar contraseña'
     */
    fun reiniciar() {
        _estadoUi.value = CambiarContrasenaUiState()
    }
    /**
     * Permite limpiar los mensajes de error de la pantalla 'Cambiar Contrasenia'
     */
    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = 0)
    }
    /**
     * Permite limpiar los mensajes de error de la pantalla 'Nueva Contrasenia'
     */
    fun limpiarErrorNueva() {
        _estadoNueva.value = _estadoNueva.value.copy(mensajeError = null)
    }
}