package com.example.voxtask.ui.screens.Contador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContadorViewModel : ViewModel() {

    //Variables
    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido
    var tiempoFormato by mutableStateOf("00:00:00")
    var mostrarContador by mutableStateOf(false)
    private var countdownJob: Job? = null

    //Funcion que recibe el texto transformado por voz y lo convierte a minusculas y elimina espacios
    fun onTextoRecibido(texto: String) {
        _textoReconocido.value = texto
        procesarComando(texto)
    }

    //Funcion para procesar el texto mediante la voz y ejecutar las acciones programadas
    private fun procesarComando(texto: String) {
        android.util.Log.d("CONTADOR", "Texto recibido: $texto")

        val textoNormalizado = normalizarNumeros(texto.lowercase().trim())
            .replace(" y ", " ") // eliminar "y" para que no interfiera
        android.util.Log.d("CONTADOR", "Texto normalizado: $textoNormalizado")

        val partes = textoNormalizado.split("\\s+".toRegex())
        var totalSegundos = 0

        partes.forEachIndexed { index, parte ->
            val numero = parte.toIntOrNull()
            if (numero != null) {
                // Buscar unidad en las siguientes 2 palabras (por si hay palabras intermedias)
                val sig1 = partes.getOrNull(index + 1)?.lowercase() ?: ""
                val sig2 = partes.getOrNull(index + 2)?.lowercase() ?: ""
                val unidad = if (sig1.toIntOrNull() != null) sig2 else sig1

                when {
                    unidad.startsWith("hora") -> totalSegundos += numero * 3600
                    unidad.startsWith("minuto") || unidad.startsWith("min") -> totalSegundos += numero * 60
                    unidad.startsWith("segundo") || unidad.startsWith("seg") -> totalSegundos += numero
                }
                android.util.Log.d("CONTADOR", "Número: $numero, Unidad: $unidad")
            }
        }

        android.util.Log.d("CONTADOR", "Total segundos calculados: $totalSegundos")
        if (totalSegundos > 0) iniciarContador(totalSegundos)
    }
    //Funcion para convertir los numeros de voz a texto
    private fun normalizarNumeros(texto: String): String {
        val reemplazos = linkedMapOf(
            "cincuenta y uno" to "51",
            "cincuenta y dos" to "52",
            "cincuenta y tres" to "53",
            "cuarenta y cinco" to "45",
            "treinta y uno" to "31",
            "veinte y uno" to "21",
            "veintiuno" to "21",
            "veintidós" to "22",
            "veintitres" to "23",
            "veintitrés" to "23",
            "veinticuatro" to "24",
            "veinticinco" to "25",
            "veintiseis" to "26",
            "veintisiete" to "27",
            "veintiocho" to "28",
            "veintinueve" to "29",
            "cincuenta" to "50",
            "cuarenta" to "40",
            "treinta" to "30",
            "veinte" to "20",
            "quince" to "15",
            "catorce" to "14",
            "trece" to "13",
            "doce" to "12",
            "once" to "11",
            "diez" to "10",
            "nueve" to "9",
            "ocho" to "8",
            "siete" to "7",
            "seis" to "6",
            "cinco" to "5",
            "cuatro" to "4",
            "tres" to "3",
            "dos" to "2",
            "uno" to "1",
            "una " to "1 ",
            "cero" to "0"
        )

        var resultado = texto
        reemplazos.forEach { (palabra, numero) ->
            resultado = resultado.replace(palabra, numero)
        }
        android.util.Log.d("CONTADOR", "Después de normalizar: $resultado")
        return resultado
    }

    //Funcion para crear el contador
    private fun iniciarContador(totalSegundos: Int) {
        mostrarContador = true
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var restantes = totalSegundos
            while (restantes >= 0) {
                val h = restantes / 3600
                val m = (restantes % 3600) / 60
                val s = restantes % 60
                tiempoFormato = String.format("%02d:%02d:%02d", h, m, s)
                if (restantes == 0) break
                delay(1000L)
                restantes--
            }
        }
    }
    //Funcion para cancelar el temporizador al destruirse el viewModel
    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}