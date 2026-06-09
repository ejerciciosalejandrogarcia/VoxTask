package com.example.voxtask.ui.screens.Inicio

import androidx.lifecycle.ViewModel
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.flow.MutableStateFlow

class InicioViewModel : ViewModel() {

    /** Variables */
    var bienvenidaDada = false
    private val _textoReconocido = MutableStateFlow("")
    var abrirContador: () -> Unit = {}
    var abrirListaCompra: () -> Unit = {}
    var abrirRecordatorio: () -> Unit = {}
    var abrirCorreo: () -> Unit = {}
    var abrirClima: () -> Unit = {}


    /** Permite recibir el texto transformado por voz y lo convierte a minusculas y elimina los espacios */
    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

    /** Permite procesar el texto mediante la voz y ejecutar las acciones programadas dependiendo del idioma seleccionado */
    private fun procesarComando(texto: String) {
        val textoMinusculas = texto.lowercase()
        val idioma = TextoAVoz.localeActual.language

        val comandoContador     = when (idioma) { "en" -> "timer"; "fr" -> "compteur"; "de" -> "zähler"; "it" -> "contatore"; "pt" -> "contador"; else -> "contador" }
        val comandoCorreo       = when (idioma) { "en" -> "email"; "fr" -> "courriel"; "de" -> "mail";   "it" -> "posta";     "pt" -> "correio";  else -> "correo" }
        val comandoRecordatorio = when (idioma) { "en" -> "reminder"; "fr" -> "rappel"; "de" -> "erinnerung"; "it" -> "promemoria"; "pt" -> "lembrete"; else -> "recordatorio" }
        val comandoLista        = when (idioma) { "en" -> "list";  "fr" -> "liste";    "de" -> "liste";  "it" -> "lista";     "pt" -> "lista";    else -> "lista" }
        val comandoClima        = when (idioma) {
            "en" -> "weather"
            "fr" -> "météo"
            "de" -> "wetter"
            "it" -> "meteo"
            "pt" -> "clima"
            else -> "clima"
        }
        when {
            textoMinusculas.contains(comandoContador)     -> abrirContador()
            textoMinusculas.contains(comandoCorreo)       -> abrirCorreo()
            textoMinusculas.contains(comandoRecordatorio) -> abrirRecordatorio()
            textoMinusculas.contains(comandoLista)        -> abrirListaCompra()
            textoMinusculas.contains(comandoClima)        -> abrirClima()
        }
    }
}