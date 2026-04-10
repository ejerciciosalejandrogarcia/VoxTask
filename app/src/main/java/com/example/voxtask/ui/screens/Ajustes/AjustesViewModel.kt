package com.example.voxtask.ui.screens.Ajustes

import android.app.Activity
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.launch

class AjustesViewModel : ViewModel() {

    //Variables
    var mostrarSelectorVoz by mutableStateOf(false)
    var vocesDisponibles by mutableStateOf<List<Voice>>(emptyList())
    var vozActual by mutableStateOf(TextoAVoz.vozElegida ?: "Por defecto")

    // Funcion que carga las voces
    fun cargarVoces(contexto: android.content.Context) {
        if (TextoAVoz.obtenerVoces().isEmpty()) {
            android.speech.tts.TextToSpeech(contexto) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    vocesDisponibles = TextoAVoz.obtenerVoces()
                        .filter { !it.isNetworkConnectionRequired }
                        .sortedBy { it.locale.getDisplayName(java.util.Locale("es", "ES")) }
                }
            }
        } else {
            vocesDisponibles = TextoAVoz.obtenerVoces()
                .filter { !it.isNetworkConnectionRequired }
                .sortedBy { it.locale.getDisplayName(java.util.Locale("es", "ES")) }
        }
    }

    //Funcion que aplica la voz seleccionada por el usuario y despues mostramos una prueba de como se escucha la voz elegida
    fun aplicarVoz(nombreVoz: String, contexto: Activity) {
        TextoAVoz.cambiarVoz(nombreVoz)
        val voz = vocesDisponibles.find { it.name == nombreVoz }
        vozActual = voz?.locale?.getDisplayName(java.util.Locale("es", "ES")) ?: nombreVoz
        mostrarSelectorVoz = false

        viewModelScope.launch {
            TextoAVoz.hablar(contexto, "Esto es una prueba")
        }

    }
}