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
    var escuchando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
}

/**
 * Esta funcion configura y gestiona el reconocimiento de voz.
 * Retorna el estado actual de la escucha y una función para activarla
 */
@Composable
fun rememberVozATexto(): Pair<VozATextoState, () -> Unit> {
    val contexto = LocalContext.current
    val estado = remember { VozATextoState() }

    val reconocedorVoz = remember {
        SpeechRecognizer.createSpeechRecognizer(contexto)
    }

    DisposableEffect(Unit) {
        reconocedorVoz.setRecognitionListener(object : RecognitionListener {
            override fun onResults(resultados: Bundle?) {
                val coincidencias = resultados?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!coincidencias.isNullOrEmpty()) {
                    estado.textoReconocido = coincidencias[0]
                }
                estado.escuchando = false
            }

            override fun onError(codigoError: Int) {
                android.util.Log.e("VOZ", "Error código: $codigoError")
                estado.error = "Error: $codigoError"
                estado.escuchando = false
            }

            override fun onReadyForSpeech(parametros: Bundle?) {
                android.util.Log.d("VOZ", "Listo para escuchar")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(resultadosParciales: Bundle?) {}
            override fun onEvent(tipoEvento: Int, parametros: Bundle?) {}
        })

        onDispose {
            reconocedorVoz.destroy()
        }
    }

    val iniciarEscucha: () -> Unit = {
        val tienePermiso = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            estado.escuchando = true
            estado.textoReconocido = ""
            val intencion = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, TextoAVoz.localeActual.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, TextoAVoz.localeActual.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, contexto.packageName)
            }
            reconocedorVoz.startListening(intencion)
        } else {
            android.util.Log.e("VOZ", "No hay permiso de micrófono")
            estado.error = "Sin permiso de micrófono"
        }
    }

    return Pair(estado, iniciarEscucha)
}