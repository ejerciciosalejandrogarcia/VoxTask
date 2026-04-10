package com.example.voxtask.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

object TextoAVoz {

    private var tts: TextToSpeech? = null
    var vozElegida: String? = null

    // Devuelve la lista de voces que tiene instaladas el movil
    fun obtenerVoces(): List<Voice> {
        return tts?.voices?.toList() ?: emptyList()
    }

    // Cambia la voz por la que el usuario ha elegido
    fun cambiarVoz(nombreVoz: String) {
        vozElegida = nombreVoz
        val voz = tts?.voices?.find { it.name == nombreVoz }
        if (voz != null) tts?.voice = voz
    }

    suspend fun hablar(context: Context, texto: String) {
        if (tts == null) {
            suspendCancellableCoroutine<Unit> { continuation ->
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale("es", "MX")
                        tts?.setSpeechRate(0.95f)
                        tts?.setPitch(1.0f)

                        vozElegida?.let { nombre ->
                            val voz = tts?.voices?.find { it.name == nombre }
                            if (voz != null) tts?.voice = voz
                        }
                    }
                    continuation.resume(Unit)
                }
            }
        }

        suspendCancellableCoroutine<Unit> { continuation ->
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { continuation.resume(Unit) }
                override fun onError(utteranceId: String?) { continuation.resume(Unit) }
            })
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "id")
        }
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}