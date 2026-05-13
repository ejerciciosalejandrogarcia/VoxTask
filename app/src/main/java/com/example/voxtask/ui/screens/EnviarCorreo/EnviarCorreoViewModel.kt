package com.example.voxtask.ui.screens.EnviarCorreo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.network.EnviarCorreoRequest
import com.example.voxtask.databases.network.N8nClient
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class PasoEnvio {
    DESTINATARIO, ASUNTO, MODO, MENSAJE, CONFIRMACION, ENVIANDO, ENVIADO, ERROR
}

class EnviarCorreoViewModel : ViewModel() {

    companion object {
        const val WEB_CLIENT_ID = "820155883821-7trt2n6ghi9hlk6m039rl376reh5vjsj.apps.googleusercontent.com"
    }

    var paso by mutableStateOf(PasoEnvio.DESTINATARIO)
    var destinatario by mutableStateOf("")
    var asunto by mutableStateOf("")
    var modo by mutableStateOf("manual")
    var mensaje by mutableStateOf("")
    var errorMensaje by mutableStateOf("")
    var accessToken by mutableStateOf("")
    var necesitaVincularGoogle by mutableStateOf(false)
    var cargandoToken by mutableStateOf(false)

    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
                .build()
        )
    }

    fun iniciar(contexto: Context) {
        viewModelScope.launch {
            val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
            val tieneScope = GoogleSignIn.hasPermissions(
                cuentaGoogle,
                Scope("https://www.googleapis.com/auth/gmail.send")
            )
            if (cuentaGoogle?.account == null || !tieneScope) {
                necesitaVincularGoogle = true
            } else {
                necesitaVincularGoogle = false
                obtenerToken(contexto)
            }
        }
    }

    fun vincularGoogle(contexto: Context, onListo: () -> Unit) {
        viewModelScope.launch {
            try {
                obtenerClienteGoogle(contexto).signOut().await()
            } catch (e: Exception) { }
            onListo()
        }
    }

    fun guardarToken(contexto: Context) {
        viewModelScope.launch {
            necesitaVincularGoogle = false
            cargandoToken = true
            obtenerToken(contexto)
            cargandoToken = false
            if (accessToken.isNotEmpty()) {
                TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_vincular_cuenta_exito))
            }
        }
    }

    private suspend fun obtenerToken(contexto: Context) {
        try {
            val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
            if (cuentaGoogle?.account != null) {
                accessToken = withContext(Dispatchers.IO) {
                    val scope = "oauth2:https://www.googleapis.com/auth/gmail.send"
                    try {
                        val tokenViejo = GoogleAuthUtil.getToken(contexto, cuentaGoogle.account!!, scope)
                        GoogleAuthUtil.clearToken(contexto, tokenViejo)
                    } catch (e: Exception) { }
                    GoogleAuthUtil.getToken(contexto, cuentaGoogle.account!!, scope)
                }
            } else {
                necesitaVincularGoogle = true
            }
        } catch (e: Exception) {
            if (e.message?.contains("consent") == true ||
                e.message?.contains("remote") == true ||
                e.javaClass.simpleName == "UserRecoverableAuthException"
            ) {
                necesitaVincularGoogle = true
            } else {
                errorMensaje = contexto.getString(R.string.txt_enviarcorreo_error_token, e.message)
                paso = PasoEnvio.ERROR
            }
        }
    }

    fun procesarVoz(texto: String, contexto: Context) {
        if (necesitaVincularGoogle || cargandoToken) return
        val textoLimpio = texto.lowercase().trim()
        viewModelScope.launch {
            when (paso) {
                PasoEnvio.DESTINATARIO -> {
                    destinatario = texto.trim()
                    paso = PasoEnvio.ASUNTO
                    TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_pregunta_asunto))
                }
                PasoEnvio.ASUNTO -> {
                    asunto = texto.trim()
                    paso = PasoEnvio.MODO
                    TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_pregunta_modo))
                }
                PasoEnvio.MODO -> {
                    modo = if (textoLimpio.contains("ia") ||
                        textoLimpio.contains("inteligencia") ||
                        textoLimpio.contains("artificial") ||
                        textoLimpio.contains("cree") ||
                        textoLimpio.contains("generar")) "ia" else "manual"
                    paso = PasoEnvio.MENSAJE
                    if (modo == "ia") {
                        TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_pregunta_mensaje_ia))
                    } else {
                        TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_pregunta_mensaje_manual))
                    }
                }
                PasoEnvio.MENSAJE -> {
                    mensaje = texto.trim()
                    paso = PasoEnvio.CONFIRMACION
                    TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_titulo_confirmacion))
                }
                else -> {}
            }
        }
    }

    fun confirmarEnvio(contexto: Context) {
        enviarCorreo(contexto)
    }

    fun editarCampo(campo: String) {
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
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                accessToken = withContext(Dispatchers.IO) {
                    val scope = "oauth2:https://www.googleapis.com/auth/gmail.send"
                    val token = GoogleAuthUtil.getToken(contexto, cuentaGoogle!!.account!!, scope)
                    GoogleAuthUtil.clearToken(contexto, token)
                    GoogleAuthUtil.getToken(contexto, cuentaGoogle.account!!, scope)
                }
            } catch (e: Exception) {
                errorMensaje = contexto.getString(R.string.txt_enviarcorreo_error_auth, e.message)
                paso = PasoEnvio.ERROR
                return@launch
            }

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
                    TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_exito))
                } else {
                    errorMensaje = contexto.getString(R.string.txt_enviarcorreo_error_generico)
                    paso = PasoEnvio.ERROR
                    TextoAVoz.hablar(contexto, errorMensaje)
                }
            } catch (e: Exception) {
                errorMensaje = e.message ?: contexto.getString(R.string.txt_enviarcorreo_error_generico)
                paso = PasoEnvio.ERROR
                TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_error_generico))
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
            TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_paso_destinatario_pregunta))
        }
    }
}