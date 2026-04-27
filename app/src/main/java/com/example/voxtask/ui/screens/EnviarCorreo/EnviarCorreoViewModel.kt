// EnviarCorreoViewModel.kt
package com.example.voxtask.ui.screens.EnviarCorreo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.network.EnviarCorreoRequest
import com.example.voxtask.databases.network.N8nClient
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PasoEnvio {
    DESTINATARIO, ASUNTO, MODO, MENSAJE, CONFIRMACION, ENVIANDO, ENVIADO, ERROR
}

class EnviarCorreoViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    var modoEdicion by mutableStateOf(false)
    var paso by mutableStateOf(PasoEnvio.DESTINATARIO)
    var destinatario by mutableStateOf("")
    var asunto by mutableStateOf("")
    var modo by mutableStateOf("manual")
    var mensaje by mutableStateOf("")
    var errorMensaje by mutableStateOf("")
    var accessToken by mutableStateOf("")

    fun iniciar(contexto: Context) {
        viewModelScope.launch {
            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                if (cuentaGoogle?.account != null) {
                    accessToken = withContext(Dispatchers.IO) {
                        val tokenActual = GoogleAuthUtil.getToken(
                            contexto,
                            cuentaGoogle.account!!,
                            "oauth2:https://www.googleapis.com/auth/gmail.send"
                        )
                        GoogleAuthUtil.clearToken(contexto, tokenActual)
                        GoogleAuthUtil.getToken(
                            contexto,
                            cuentaGoogle.account!!,
                            "oauth2:https://www.googleapis.com/auth/gmail.send"
                        )
                    }
                }
            } catch (e: Exception) {
                errorMensaje = "Error obteniendo token: ${e.message}"
                paso = PasoEnvio.ERROR
            }
        }
    }

    fun procesarVoz(texto: String, contexto: Context) {
        val textoLimpio = texto.lowercase().trim()
        viewModelScope.launch {
            when (paso) {
                PasoEnvio.DESTINATARIO -> {
                    destinatario = texto.trim()
                    paso = PasoEnvio.ASUNTO
                    TextoAVoz.hablar(contexto, "¿Cuál es el asunto del correo?")
                }
                PasoEnvio.ASUNTO -> {
                    asunto = texto.trim()
                    paso = PasoEnvio.MODO
                    TextoAVoz.hablar(contexto, "¿Quieres escribir el mensaje tú, o que lo cree la inteligencia artificial?")
                }
                PasoEnvio.MODO -> {
                    modo = if (textoLimpio.contains("ia") ||
                        textoLimpio.contains("inteligencia") ||
                        textoLimpio.contains("artificial") ||
                        textoLimpio.contains("cree") ||
                        textoLimpio.contains("generar")) "ia" else "manual"
                    paso = PasoEnvio.MENSAJE
                    if (modo == "ia") {
                        TextoAVoz.hablar(contexto, "¿Sobre qué quieres que trate el correo?")
                    } else {
                        TextoAVoz.hablar(contexto, "Dicta el mensaje del correo.")
                    }
                }
                PasoEnvio.MENSAJE -> {
                    mensaje = texto.trim()
                    paso = PasoEnvio.CONFIRMACION
                    TextoAVoz.hablar(contexto, "Revisa los datos. ¿Todo correcto?")
                }
                else -> {}
            }
        }
    }

    fun confirmarEnvio(contexto: Context) {
        enviarCorreo(contexto)
    }

    fun editarCampo(campo: String) {
        // formato "campo:nuevoValor" para actualizaciones, o solo "campo" para navegar
        if (campo.contains(":")) {
            val (nombre, valor) = campo.split(":", limit = 2)
            when (nombre) {
                "destinatario" -> destinatario = valor
                "asunto" -> asunto = valor
                "mensaje" -> mensaje = valor
            }
        }
    }
    private fun enviarCorreo(contexto: Context) {
        viewModelScope.launch {
            paso = PasoEnvio.ENVIANDO
            try {
                val request = EnviarCorreoRequest(
                    token = accessToken,
                    para = destinatario,
                    asunto = asunto,
                    mensaje = mensaje,
                    modo = modo
                )
                val response = N8nClient.api.enviarCorreo(request)
                if (response.isSuccessful) {
                    paso = PasoEnvio.ENVIADO
                    TextoAVoz.hablar(contexto, "Correo enviado correctamente.")
                } else {
                    errorMensaje = "Error al enviar: ${response.code()}"
                    paso = PasoEnvio.ERROR
                    TextoAVoz.hablar(contexto, "Hubo un error al enviar el correo.")
                }
            } catch (e: Exception) {
                errorMensaje = "Error: ${e.message}"
                paso = PasoEnvio.ERROR
                TextoAVoz.hablar(contexto, "Hubo un error al enviar el correo.")
            }
        }
    }

    fun reiniciar(contexto: Context) {
        destinatario = ""
        asunto = ""
        modo = "manual"
        mensaje = ""
        errorMensaje = ""
        paso = PasoEnvio.DESTINATARIO
        viewModelScope.launch {
            TextoAVoz.hablar(contexto, "¿A quién se lo quieres enviar?")
        }
    }

    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        val emailFirebase = FirebaseAuth.getInstance().currentUser?.email ?: ""
        return GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
                .setAccountName(emailFirebase)
                .build()
        )
    }

    fun guardarToken(contexto: Context) {
        iniciar(contexto)
    }
}