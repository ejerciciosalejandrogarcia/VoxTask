package com.example.voxtask.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

object TextoAVoz {

    private var tts: TextToSpeech? = null

    suspend fun hablar(context: Context, texto: String) {
        // Inicializar si es necesario
        if (tts == null) {
            suspendCancellableCoroutine<Unit> { continuation ->
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale("es", "MX")
                        tts?.setPitch(1.0f)
                        tts?.setSpeechRate(0.9f)
                    }
                    continuation.resume(Unit)
                }
            }
        }

        // Hablar y esperar
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