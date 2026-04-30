package com.example.voxtask.ui.screens.Verificacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String = "",
    val verificado: Boolean = false
)

class VerificacionViewModel : ViewModel() {

    private val _estadoUi = MutableStateFlow(VerificacionUiState())
    val estadoUi: StateFlow<VerificacionUiState> = _estadoUi.asStateFlow()

    init {
        enviarCodigo()
    }

    fun enviarCodigo() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val email = usuario.email ?: return
        val uid = usuario.uid

        _estadoUi.value = _estadoUi.value.copy(
            email = email,
            cargando = true,
            error = "",
            verificado = false,
            codigoCorrecto = ""
        )

        viewModelScope.launch {
            try {
                val respuesta = N8nClient.api.enviarCodigoVerificacion(
                    VerificacionRequest(email = email, uid = uid)
                )
                android.util.Log.d("Verificacion", "Código recibido: '${respuesta.codigo}'")
                _estadoUi.value = _estadoUi.value.copy(
                    codigoCorrecto = respuesta.codigo,

                    cargando = false
                )
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(
                    error = "Error al enviar el código: ${e.message}",
                    cargando = false
                )
            }
        }
    }

    fun verificarCodigo(codigoIntroducido: String) {
        val introducido = codigoIntroducido.trim().uppercase()
        val correcto = _estadoUi.value.codigoCorrecto.trim().uppercase()

        if (introducido == correcto) {
            _estadoUi.value = _estadoUi.value.copy(verificado = true, error = "")
        } else {
            _estadoUi.value = _estadoUi.value.copy(error = "Código incorrecto, inténtalo de nuevo")
        }
    }
}