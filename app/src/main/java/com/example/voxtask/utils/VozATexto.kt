package com.example.voxtask.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest

/**
 * Almacena el estado actual del servicio de voz
 */
class VozATextoState {
    var textoReconocido by mutableStateOf("")
    var isListening by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
}

/**
 * Esta funcion configura y gestiona el reconocimiento de voz.
 * Retorna el estado actual de la escucha y una función para activarla
 */
    @Composable
    fun rememberVozATexto(): Pair<VozATextoState, () -> Unit> {
        val context = LocalContext.current
        val state = remember { VozATextoState() }

        val speechRecognizer = remember {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        DisposableEffect(Unit) {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        state.textoReconocido = matches[0]
                    }
                    state.isListening = false
                }

                override fun onError(error: Int) {
                    android.util.Log.e("VOZ", "Error código: $error")
                    state.error = "Error: $error"
                    state.isListening = false
                }

                override fun onReadyForSpeech(params: Bundle?) {
                    android.util.Log.d("VOZ", "Listo para escuchar")
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            onDispose {
                speechRecognizer.destroy()
            }
        }

        val iniciarEscucha: () -> Unit = {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (tienePermiso) {
                state.isListening = true
                state.textoReconocido = ""
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, TextoAVoz.localeActual.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, TextoAVoz.localeActual.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                speechRecognizer.startListening(intent)
            } else {
                android.util.Log.e("VOZ", "No hay permiso de micrófono")
                state.error = "Sin permiso de micrófono"
            }
        }

        return Pair(state, iniciarEscucha)
    }