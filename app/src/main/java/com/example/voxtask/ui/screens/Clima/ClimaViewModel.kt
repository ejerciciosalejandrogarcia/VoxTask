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

/**
 * Representa el estado de la UI
 */
data class ClimaUiState(
    val datos: ClimaResponse? = null,
    val estaCargando: Boolean = false,
    val sinUbicacion: Boolean = true,
    val mensajeErrorResId: Int? = null,
    val errorMensajeDinamico: String? = null
)

class ClimaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClimaUiState())
    val uiState: StateFlow<ClimaUiState> = _uiState.asStateFlow()

    /** Permite obtener la informacion del clima dependiendo de la ubicacion del usuario */
    fun cargarClima(lat: Double, lon: Double) {
        viewModelScope.launch {
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
                _uiState.value = _uiState.value.copy(
                    estaCargando = false,
                    mensajeErrorResId = R.string.clima_error,
                    errorMensajeDinamico = e.message
                )
            }
        }
    }

    /**
     * Permite limpiar los mensajes de error de la pantalla 'Clima'
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(
            mensajeErrorResId = null,
            errorMensajeDinamico = null
        )
    }

    /** Permite actualizar la UI si no esta activado la ubicacion */
    fun establecerSinUbicacion() {
        _uiState.value = _uiState.value.copy(
            sinUbicacion = true,
            estaCargando = false
        )
    }

}