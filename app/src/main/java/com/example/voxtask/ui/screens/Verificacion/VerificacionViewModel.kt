package com.example.voxtask.ui.screens.Verificacion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.network.N8nClient
import com.example.voxtask.databases.network.VerificacionRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerificacionUiState(
    val email: String = "",
    val codigoCorrecto: String = "",
    val cargando: Boolean = false,
    val mensajeError: Int? = null,
    val errorDinamico: String = "",
    val verificado: Boolean = false,
    val errorEnvio: Boolean = false
)

class VerificacionViewModel : ViewModel() {

    private val _estadoUi = MutableStateFlow(VerificacionUiState())
    val estadoUi: StateFlow<VerificacionUiState> = _estadoUi.asStateFlow()

    init {
        enviarCodigo()
    }

    fun enviarCodigo(contexto: Context? = null) {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val email = usuario.email ?: return
        val uid = usuario.uid

        _estadoUi.value = _estadoUi.value.copy(
            email = email,
            cargando = true,
            mensajeError = null,
            errorDinamico = "",
            verificado = false,
            codigoCorrecto = "",
            errorEnvio = false
        )

        viewModelScope.launch {
            try {
                val respuesta = N8nClient.api.enviarCodigoVerificacion(
                    VerificacionRequest(email = email, uid = uid)
                )
                android.util.Log.d("Verificacion", "Código recibido: '${respuesta.codigo}'")
                _estadoUi.value = _estadoUi.value.copy(
                    codigoCorrecto = respuesta.codigo,
                    cargando = false,
                    errorEnvio = false
                )
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    errorDinamico = contexto?.getString(R.string.error_enviar_codigo, e.message ?: "")
                        ?: "Error al enviar el código: ${e.message}",
                    cargando = false,
                    errorEnvio = true
                )
            }
        }
    }

    fun verificarCodigo(codigoIntroducido: String) {
        val introducido = codigoIntroducido.trim().uppercase()
        val correcto = _estadoUi.value.codigoCorrecto.trim().uppercase()

        if (introducido == correcto) {
            _estadoUi.value = _estadoUi.value.copy(verificado = true, mensajeError = null)
        } else {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_codigo_incorrecto)
        }
    }

    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null, errorDinamico = "")
    }
}