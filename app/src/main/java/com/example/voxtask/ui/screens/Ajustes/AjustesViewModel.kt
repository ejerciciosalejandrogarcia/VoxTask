package com.example.voxtask.ui.screens.Ajustes

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.launch
import java.util.Locale

class AjustesViewModel : ViewModel() {

    //Variables
    var mostrarSelectorVoz by mutableStateOf(false)
    var vocesDisponibles by mutableStateOf<List<Voice>>(emptyList())
    var vozActual by mutableStateOf(TextoAVoz.vozElegida ?: "Por defecto")
    var mostrarSelectorIdioma by mutableStateOf(false)
    var idiomaActual by mutableStateOf("Español")

    val idiomasDisponibles = listOf(
        Pair("es", "Español"),
        Pair("en", "English"),
        Pair("fr", "Français"),
        Pair("de", "Deutsch"),
        Pair("it", "Italiano"),
        Pair("pt", "Português")
    )


    // Funcion que carga las voces y los textos
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


    fun cargarIdiomas(contexto: Context, idioma: String, onListo: () -> Unit) {
        val locale = java.util.Locale(idioma)
        java.util.Locale.setDefault(locale)

        val config = Configuration(contexto.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        contexto.resources.updateConfiguration(config, contexto.resources.displayMetrics)

        // commit() garantiza que se guarda antes de continuar
        val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        prefs.edit().putString("idioma", idioma).commit()

        idiomaActual = idiomasDisponibles.find { it.first == idioma }?.second ?: idioma

        onListo() // avisa a la pantalla que ya está listo
    }
    fun inicializarIdioma(contexto: Context) {
        val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = prefs.getString("idioma", "es") ?: "es"
        idiomaActual = idiomasDisponibles.find { it.first == idioma }?.second ?: "Español"
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