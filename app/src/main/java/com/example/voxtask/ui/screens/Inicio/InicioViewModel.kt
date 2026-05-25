package com.example.voxtask.ui.screens.Inicio


import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InicioViewModel : ViewModel() {


    //Variables
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    var bienvenidaDada = false
    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido
    var abrirContador: () -> Unit = {}
    var abrirListaCompra: () -> Unit = {}
    var abrirRecordatorio: () -> Unit = {}
    var abrirCorreo: () -> Unit = {}
    var abrirClima: () -> Unit = {}


    //Funcion que recibe el texto transformado por voz y lo convierte a minusculas y elimina espacios
    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

    //Funcion para procesar el texto mediante la voz y ejecutar las acciones programadas
    private fun procesarComando(texto: String) {
        val textoLower = texto.lowercase()
        val idioma = TextoAVoz.localeActual.language

        val comandoContador    = when (idioma) { "en" -> "timer"; "fr" -> "compteur"; "de" -> "zähler"; "it" -> "contatore"; "pt" -> "contador"; else -> "contador" }
        val comandoCorreo      = when (idioma) { "en" -> "email";   "fr" -> "courriel"; "de" -> "mail";   "it" -> "posta";     "pt" -> "correio";  else -> "correo" }
        val comandoRecordatorio = when (idioma) { "en" -> "reminder"; "fr" -> "rappel"; "de" -> "erinnerung"; "it" -> "promemoria"; "pt" -> "lembrete"; else -> "recordatorio" }
        val comandoLista       = when (idioma) { "en" -> "list";    "fr" -> "liste";    "de" -> "liste";  "it" -> "lista";     "pt" -> "lista";    else -> "lista" }
        val comandoClima = when (idioma) {
            "en" -> "weather"
            "fr" -> "météo"
            "de" -> "wetter"
            "it" -> "meteo"
            "pt" -> "clima"
            else -> "clima"
        }
        when {
            textoLower.contains(comandoContador)     -> abrirContador()
            textoLower.contains(comandoCorreo)       -> abrirCorreo()
            textoLower.contains(comandoRecordatorio) -> abrirRecordatorio()
            textoLower.contains(comandoLista)        -> abrirListaCompra()
            textoLower.contains(comandoClima)        -> abrirClima()

        }
    }

    fun cerrarSesion(contexto: Context, actividad: Activity, alCerrar: () -> Unit) {
        val uid = auth.currentUser?.uid

        // Borrar el gmailAccessToken de Firestore
        if (uid != null) {
            firestore.collection("usuarios")
                .document(uid)
                .update("gmailAccessToken", "")
        }

        // Cerrar sesión de Google primero, luego Firebase
        val clienteGoogle = GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )

        clienteGoogle.signOut().addOnCompleteListener {
            auth.signOut()
            alCerrar()
            actividad.finishAffinity()
        }
    }
}