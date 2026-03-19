package com.example.voxtask.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

object TextoAVoz {

    private var motorVoz: TextToSpeech? = null
    private var listo = false

    fun hablar(contexto: Context, texto: String) {
        if (motorVoz == null) {

            motorVoz = TextToSpeech(contexto) { estado ->
                if (estado == TextToSpeech.SUCCESS) {
                    motorVoz?.language = Locale("es", "MX")
                    motorVoz?.setPitch(1.0f)
                    motorVoz?.setSpeechRate(0.9f)
                    listo = true
                    motorVoz?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
                    Log.d("Voz", "✅ Hablando: $texto")
                } else {
                    Log.e("Voz", "❌ Error")
                }
            }
        } else if (listo) {
            motorVoz?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("Voz", "✅ Hablando: $texto")
        } else {
            Log.d("Voz", "⏳ Motor no listo aún")
        }
    }

    fun liberar() {
        motorVoz?.stop()
        motorVoz?.shutdown()
        motorVoz = null
        listo = false
    }
}