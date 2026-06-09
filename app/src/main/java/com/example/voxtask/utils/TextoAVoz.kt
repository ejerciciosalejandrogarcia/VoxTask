package com.example.voxtask.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

object TextoAVoz {
    /** Variables */
    private var motorVoz: TextToSpeech? = null
    var vozElegida: String? = null
    var localeActual: Locale = Locale("es", "MX")

    /** Permite devolver la lista de voces que tiene instaladas el movil */
    fun obtenerVoces(): List<Voice> {
        return motorVoz?.voices?.toList() ?: emptyList()
    }

    /** Permite cambiar la voz por la que el usuario ha elegido */
    fun cambiarVoz(nombreVoz: String) {
        vozElegida = nombreVoz
        val voz = motorVoz?.voices?.find { it.name == nombreVoz }
        if (voz != null) motorVoz?.voice = voz
    }
    /** Permite cambiar el idioma por la que el usuario ha elegido */
    fun cambiarIdioma(locale: Locale) {
        localeActual = locale
        motorVoz?.language = locale
        vozElegida = null
    }

    /**
     * Permite convertir texto a voz de forma asíncrona,
     * inicializa el motor TTS y suspende la ejecución,hasta que la lectura del texto haya terminado
     */
    suspend fun hablar(contexto: Context, texto: String) {
        if (motorVoz == null) {
            suspendCancellableCoroutine<Unit> { continuacion ->
                motorVoz = TextToSpeech(contexto) { estado ->
                    if (estado == TextToSpeech.SUCCESS) {
                        motorVoz?.language = localeActual
                        motorVoz?.setSpeechRate(0.95f)
                        motorVoz?.setPitch(1.0f)

                        vozElegida?.let { nombre ->
                            val voz = motorVoz?.voices?.find { it.name == nombre }
                            if (voz != null) motorVoz?.voice = voz
                        }
                    }
                    continuacion.resume(Unit)
                }
            }
        }

        suspendCancellableCoroutine<Unit> { continuacion ->
            motorVoz?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { continuacion.resume(Unit) }
                override fun onError(utteranceId: String?) { continuacion.resume(Unit) }
            })
            motorVoz?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "id")
        }
    }
}