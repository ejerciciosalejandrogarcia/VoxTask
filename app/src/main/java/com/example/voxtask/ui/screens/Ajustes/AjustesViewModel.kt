package com.example.voxtask.ui.screens.Ajustes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.speech.tts.Voice
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AjustesViewModel : ViewModel() {

    /** Variables */
    var mostrarSelectorVoz by mutableStateOf(false)
    var vocesDisponibles by mutableStateOf<List<Voice>>(emptyList())
    var vozActual by mutableStateOf(TextoAVoz.vozElegida ?: "Por defecto")
    var mostrarSelectorIdioma by mutableStateOf(false)
    var idiomaActual by mutableStateOf("Español")
    var mostrarSelectorColor by mutableStateOf(false)
    var idiomaVozActual by mutableStateOf("")
    val idiomasDisponibles = listOf(
        Pair("es", "Español"),
        Pair("en", "English"),
        Pair("fr", "Français"),
        Pair("de", "Deutsch"),
        Pair("it", "Italiano"),
        Pair("pt", "Português")
    )


    /** Permite cargar las voces disponibles */
    fun cargarVoces(contexto: android.content.Context) {
        val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idiomaActualCodigo = prefs.getString("idioma", "es") ?: "es"

        val filtrarVoces: (List<Voice>) -> List<Voice> = { voces ->
            voces
                .filter {
                    !it.isNetworkConnectionRequired &&
                            it.locale.language == idiomaActualCodigo
                }
                .distinctBy { it.locale.country }
                .sortedBy { it.locale.getDisplayName(java.util.Locale("es", "ES")) }
        }

        if (TextoAVoz.obtenerVoces().isEmpty()) {
            android.speech.tts.TextToSpeech(contexto) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    vocesDisponibles = filtrarVoces(TextoAVoz.obtenerVoces())
                }
            }
        } else {
            vocesDisponibles = filtrarVoces(TextoAVoz.obtenerVoces())
        }
    }

    /** Permite cargar los idiomas disponibles */
    fun cargarIdiomas(contexto: Context, idioma: String, onListo: () -> Unit) {
        val locale = Locale(idioma)
        Locale.setDefault(locale)

        val config = Configuration(contexto.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        contexto.resources.updateConfiguration(config, contexto.resources.displayMetrics)

        val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        prefs.edit().putString("idioma", idioma).commit()

        idiomaActual = idiomasDisponibles.find { it.first == idioma }?.second ?: idioma

        viewModelScope.launch {
            TextoAVoz.cambiarIdioma(locale)
            val mensajePrueba = when (idioma) {
                "en" -> "Language changed to English"
                "fr" -> "Langue changée en français"
                "de" -> "Sprache auf Deutsch geändert"
                "it" -> "Lingua cambiata in italiano"
                "pt" -> "Idioma alterado para português"
                else -> "Idioma cambiado a español"
            }
            TextoAVoz.hablar(contexto, mensajePrueba)
        }

        onListo()
    }
    /** Permite que la aplicación mantenga el idioma seleccionado */
    fun inicializarIdioma(contexto: Context) {
        val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = prefs.getString("idioma", "es") ?: "es"
        idiomaActual = idiomasDisponibles.find { it.first == idioma }?.second ?: "Español"
    }

    /** Permite actualizar la voz seleccionada y la voz seleccionada dice la frase 'Esto es una prueba' segun el idioma seleccionado */
    fun aplicarVoz(nombreVoz: String, contexto: Activity) {
        TextoAVoz.cambiarVoz(nombreVoz)
        val voz = vocesDisponibles.find { it.name == nombreVoz }
        vozActual = voz?.locale?.getDisplayName(java.util.Locale("es", "ES")) ?: nombreVoz
        mostrarSelectorVoz = false

        viewModelScope.launch {
            val mensajePrueba = when (voz?.locale?.language) {
                "en" -> "This is a test"
                "fr" -> "Ceci est un test"
                "de" -> "Das ist ein Test"
                "it" -> "Questo è un test"
                "pt" -> "Isso é um teste"
                else -> "Esto es una prueba"
            }
            TextoAVoz.hablar(contexto, mensajePrueba)
        }
    }

    /** Permite compartir el archivo APK de la aplicación mediante el selector de archivos */
    fun compartirAplicacion(contexto: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apkCompartido = File(contexto.cacheDir, "VoxTask.apk")
                val archivos = contexto.assets.list("")
                Log.d("ASSETS", "Archivos disponibles: ${archivos?.joinToString()}")

                contexto.assets.open("VoxTask.apk").use { input ->
                    apkCompartido.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                apkCompartido.setReadable(true, false)

                val uri = FileProvider.getUriForFile(
                    contexto,
                    "${contexto.packageName}.provider",
                    apkCompartido
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Te comparto VoxTask, ¡instálala!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                withContext(Dispatchers.Main) {
                    contexto.startActivity(
                        Intent.createChooser(intent, "Compartir VoxTask")
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(contexto, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}