package com.example.voxtask.ui.screens.Clima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.network.ClimaResponse
import com.example.voxtask.databases.network.N8nClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Redefinimos los estados: Ahora las alertas no destruyen la UI del clima actual
data class ClimaUiState(
    val datos: ClimaResponse? = null,
    val estaCargando: Boolean = false,
    val sinUbicacion: Boolean = true,
    val mensajeErrorResId: Int? = null,        // Para strings de error traducibles (R.string...)
    val errorMensajeDinamico: String? = null   // Para capturar excepciones directas del servidor/SDK
)

class ClimaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClimaUiState())
    val uiState: StateFlow<ClimaUiState> = _uiState.asStateFlow()

    fun cargarClima(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Activamos la carga sin borrar los datos anteriores (así evitamos parpadeos molestos)
            _uiState.value = _uiState.value.copy(
                estaCargando = true,
                sinUbicacion = false,
                mensajeErrorResId = null,
                errorMensajeDinamico = null
            )

            try {
                val respuesta = N8nClient.api.obtenerClima(lat, lon)
                _uiState.value = _uiState.value.copy(
                    datos = respuesta,
                    estaCargando = false
                )
            } catch (e: Exception) {
                // Capturamos el fallo en las variables de alerta sin alterar los 'datos' que ya se veían en pantalla
                _uiState.value = _uiState.value.copy(
                    estaCargando = false,
                    mensajeErrorResId = R.string.clima_error,
                    errorMensajeDinamico = e.message ?: "Unknown error"
                )
            }
        }
    }

    // Al igual que en el login, este método resetea la alerta tras mostrarse en el Snackbar superior
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(
            mensajeErrorResId = null,
            errorMensajeDinamico = null
        )
    }

    // Si el usuario deniega los permisos de localización de forma definitiva
    fun establecerSinUbicacion() {
        _uiState.value = _uiState.value.copy(
            sinUbicacion = true,
            estaCargando = false
        )
    }
}