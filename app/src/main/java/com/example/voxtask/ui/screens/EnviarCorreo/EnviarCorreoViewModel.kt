package com.example.voxtask.ui.screens.EnviarCorreo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.network.EnviarCorreoRequest
import com.example.voxtask.databases.network.ClienteN8n
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Define las diferentes etapas del proceso de envío de un correo electrónico
 */
enum class PasoEnvio {
    DESTINATARIO, ASUNTO, MODO, MENSAJE, CONFIRMACION, ENVIANDO, ENVIADO, ERROR
}

class EnviarCorreoViewModel : ViewModel() {

    /**
     * Permite autenticar la aplicación ante los servicios de Google Cloud Platform.
     */
    companion object {
        const val ID_CLIENTE_WEB = "820155883821-7trt2n6ghi9hlk6m039rl376reh5vjsj.apps.googleusercontent.com"
    }
    /** Variables */
    var paso          by mutableStateOf(PasoEnvio.DESTINATARIO)
    var destinatario  by mutableStateOf("")
    var asunto        by mutableStateOf("")
    var modo          by mutableStateOf("manual")
    var mensaje       by mutableStateOf("")
    var tokenAcceso   by mutableStateOf("")
    var necesitaVincularGoogle by mutableStateOf(false)
    var cargandoToken by mutableStateOf(false)

    private val _canalError = Channel<String>(Channel.BUFFERED)
    val flujoError = _canalError.receiveAsFlow()

    /**
     * Permite devolver el cliente de inicio de sesión configurado para leer y enviar
     * correos electrónicos mediante la API de Gmail.
     */
    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ID_CLIENTE_WEB)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
                .build()
        )
    }
    /**
     * Permite verificar si el usuario ha iniciado sesión y si posee los permisos necesarios
     * para interactuar con la API de Gmail
     */
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

    /**
     * Permite cerrar cualquier sesión de Google activa para asegurar que el proceso de
     * vinculación comience desde 0 y el usuario pueda seleccionar
     * su cuenta sin problemas
     */
    fun vincularGoogle(contexto: Context, alTerminar: () -> Unit) {
        viewModelScope.launch {
            try {
                obtenerClienteGoogle(contexto).signOut().await()
            } catch (e: Exception) { }
            alTerminar()
        }
    }

    /**
     * Permite gestionar el flujo de vinculación con Google, controlando los estados de carga
     * y avisarle al usuario tras completar el proceso.
     */
    fun guardarToken(contexto: Context) {
        viewModelScope.launch {
            necesitaVincularGoogle = false
            cargandoToken = true
            obtenerToken(contexto)
            cargandoToken = false
            if (tokenAcceso.isNotEmpty()) {
                TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_vincular_cuenta_exito))
            }
        }
    }

    /**
     * Permite obtener un token de acceso fresco para la API de Gmail.
     */
    private suspend fun obtenerToken(contexto: Context) {
        try {
            val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
            if (cuentaGoogle?.account != null) {
                tokenAcceso = withContext(Dispatchers.IO) {
                    val alcance = "oauth2:https://www.googleapis.com/auth/gmail.send"
                    try {
                        val tokenViejo = GoogleAuthUtil.getToken(contexto, cuentaGoogle.account!!, alcance)
                        GoogleAuthUtil.clearToken(contexto, tokenViejo)
                    } catch (e: Exception) { }
                    GoogleAuthUtil.getToken(contexto, cuentaGoogle.account!!, alcance)
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
                _canalError.send(contexto.getString(R.string.txt_enviarcorreo_error_token, e.message))
                paso = PasoEnvio.ERROR
            }
        }
    }

    /**
     * Permite procesar la entrada de voz del usuario para la configuracion del nuevo correo
     */
    fun procesarVoz(texto: String, contexto: Context) {
        if (necesitaVincularGoogle || cargandoToken) return
        val textoLimpio = texto.lowercase().trim()
        val idioma = TextoAVoz.localeActual.language

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
                    val comandosIa = when (idioma) {
                        "en" -> listOf("ai", "artificial", "intelligence", "generate", "create")
                        "fr" -> listOf("ia", "intelligence", "artificielle", "générer", "créer")
                        "de" -> listOf("ki", "künstlich", "intelligenz", "generieren", "erstellen")
                        "it" -> listOf("ia", "intelligenza", "artificiale", "generare", "creare")
                        "pt" -> listOf("ia", "inteligência", "artificial", "gerar", "criar")
                        else -> listOf("ia", "inteligencia", "artificial", "cree", "generar")
                    }
                    modo = if (comandosIa.any { textoLimpio.contains(it) }) "ia" else "manual"
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

    /**
     * Permite enviar el correo configurado tras la confirmación del usuario.
     */
    fun confirmarEnvio(contexto: Context) {
        enviarCorreo(contexto)
    }

    /**
     * Permite actualizar el valor de un campo específico del correo electrónico
     */
    fun editarCampo(campo: String) {
        if (campo.contains(":")) {
            val (nombre, valor) = campo.split(":", limit = 2)
            when (nombre) {
                "destinatario" -> destinatario = valor
                "asunto"       -> asunto = valor
                "mensaje"      -> mensaje = valor
            }
        }
    }

    /**
     * Permite el envio del correo configurado
     */
    fun enviarCorreo(contexto: Context) {
        viewModelScope.launch {
            paso = PasoEnvio.ENVIANDO

            obtenerToken(contexto)

            if (paso == PasoEnvio.ERROR || necesitaVincularGoogle) return@launch
            if (tokenAcceso.isEmpty()) {
                _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.txt_enviarcorreo_error_auth, "Token vacío"))
                paso = PasoEnvio.ERROR
                return@launch
            }

            try {
                android.util.Log.d("TOKEN_ENVIO", "Token a enviar: ${tokenAcceso.take(30)}")
                android.util.Log.d("TOKEN_ENVIO", "Para: $destinatario")

                val prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
                val idiomaActual = prefs.getString("idioma", "es") ?: "es"

                val peticion = EnviarCorreoRequest(
                    token  = tokenAcceso,
                    para   = destinatario,
                    asunto = asunto,
                    mensaje = mensaje,
                    modo   = modo,
                    idioma = idiomaActual
                )
                val respuesta = ClienteN8n.api.enviarCorreo(peticion)
                if (respuesta.isSuccessful) {
                    paso = PasoEnvio.ENVIADO
                    TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_exito))
                } else {
                    val mensajeError = contexto.getString(R.string.txt_enviarcorreo_error_generico)
                    _canalError.send(contexto.getString(R.string.txt_error)+mensajeError)
                    paso = PasoEnvio.ERROR
                    TextoAVoz.hablar(contexto, mensajeError)
                }
            } catch (e: Exception) {
                val mensajeError = e.message ?: contexto.getString(R.string.txt_error)+contexto.getString(R.string.txt_enviarcorreo_error_generico)
                _canalError.send(mensajeError)
                paso = PasoEnvio.ERROR
                TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_error_generico))
            }
        }
    }
    /**
     * Permite que el formulario se restablezca a su estado inicial
     */
    fun reiniciar(contexto: Context) {
        destinatario = ""
        asunto       = ""
        modo         = "manual"
        mensaje      = ""
        paso         = PasoEnvio.DESTINATARIO
        viewModelScope.launch {
            TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_paso_destinatario_pregunta))
        }
    }
}