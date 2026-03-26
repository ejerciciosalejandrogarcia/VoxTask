package com.example.voxtask.ui.screens.Inicio


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InicioViewModel : ViewModel() {

    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido

    var abrirContador: () -> Unit = {}

    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

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

    private fun abrirRecordatorio() {

    }

    private fun abrirListaCompra() {

    }
}