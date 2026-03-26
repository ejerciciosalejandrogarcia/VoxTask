package com.example.voxtask.ui.screens.Contador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ContadorViewModel : ViewModel() {
    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido
    var tiempoFormato by mutableStateOf("00:00:00")
    var mostrarContador by mutableStateOf(false)

    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

    var abrirContador: (Int) -> Unit = {} // ✅ recibe los segundos

    private fun procesarComando(texto: String) {
        android.util.Log.d("CONTADOR", "Texto recibido: $texto")
        val partes = texto.split(" ")

        var totalSegundos = 0

        partes.forEachIndexed { index, parte ->
            val numero = parte.toIntOrNull()
            if (numero != null) {

                val siguiente = partes.getOrNull(index + 1)?.lowercase() ?: ""

                when {
                    siguiente.contains("hora") || siguiente.contains("h") -> {
                        totalSegundos += numero * 3600 // horas a segundos
                    }
                    siguiente.contains("minuto") || siguiente.contains("min") -> {
                        totalSegundos += numero * 60 // minutos a segundos
                    }
                    siguiente.contains("segundo") || siguiente.contains("seg") -> {
                        totalSegundos += numero // segundos
                    }
                }
            }
        }
        android.util.Log.d("CONTADOR", "Total segundos: $totalSegundos")
        if (totalSegundos > 0) {
            iniciarContador(totalSegundos)
        }
    }

    private fun iniciarContador(totalSegundos: Int) {
        val horas = totalSegundos / 3600
        val minutos = (totalSegundos % 3600) / 60
        val segundos = totalSegundos % 60
        tiempoFormato = String.format("%02d:%02d:%02d", horas, minutos, segundos)
        mostrarContador = true
    }



}
