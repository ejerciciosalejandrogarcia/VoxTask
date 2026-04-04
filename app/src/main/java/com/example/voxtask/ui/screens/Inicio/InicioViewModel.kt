package com.example.voxtask.ui.screens.Inicio


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InicioViewModel : ViewModel() {

    //Variables
    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido
    var abrirContador: () -> Unit = {}
    var abrirListaCompra: () -> Unit = {}
    var abrirRecordatorio: () -> Unit = {}

    //Funcion que recibe el texto transformado por voz y lo convierte a minusculas y elimina espacios
    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

    //Funcion para procesar el texto mediante la voz y ejecutar las acciones programadas
    private fun procesarComando(texto: String) {
        when {
            texto.contains("contador", ignoreCase = true) -> abrirContador()
            texto.contains("correo", ignoreCase = true) -> abrirCorreo()
            texto.contains("recordatorio", ignoreCase = true) -> abrirRecordatorio()
            texto.contains("lista", ignoreCase = true) -> abrirListaCompra()
        }
    }


    private fun abrirCorreo() {

    }
}