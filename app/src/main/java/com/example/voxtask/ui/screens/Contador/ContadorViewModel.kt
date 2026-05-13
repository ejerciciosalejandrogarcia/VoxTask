package com.example.voxtask.ui.screens.Contador

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.services.ContadorService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContadorViewModel : ViewModel() {

    private val _textoReconocido = MutableStateFlow("")
    val textoReconocido: StateFlow<String> = _textoReconocido
    var tiempoFormato by mutableStateOf("00:00:00")
    var mostrarContador by mutableStateOf(false)
    private var countdownJob: Job? = null
    var corriendo by mutableStateOf(false)
        private set

    fun restaurarSiServicioActivo() {

        if (ContadorService.segundosRestantes > 0) {

            mostrarContador = true

            if (ContadorService.estaActivo) {
                if (!corriendo) {
                    iniciarContador(ContadorService.segundosRestantes)
                }

            } else if (ContadorService.estaPausado) {
                corriendo = false

                val h = ContadorService.segundosRestantes / 3600
                val m = (ContadorService.segundosRestantes % 3600) / 60
                val s = ContadorService.segundosRestantes % 60

                tiempoFormato = String.format("%02d:%02d:%02d", h, m, s)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTextoRecibido(texto: String, contexto: Context) {
        _textoReconocido.value = texto
        procesarComando(texto, contexto)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun procesarComando(texto: String, contexto: Context) {
        android.util.Log.d("CONTADOR", "Texto recibido: $texto")

        val textoNormalizado = normalizarNumeros(texto.lowercase().trim())
            .replace(" y ", " ")
        android.util.Log.d("CONTADOR", "Texto normalizado: $textoNormalizado")

        val partes = textoNormalizado.split("\\s+".toRegex())
        var totalSegundos = 0

        partes.forEachIndexed { index, parte ->
            val numero = parte.toIntOrNull()
            if (numero != null) {
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
        if (totalSegundos > 0) {
            iniciarContadorConServicio(contexto, totalSegundos)
            iniciarContador(totalSegundos)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun iniciarContadorConServicio(contexto: Context, totalSegundos: Int) {
        val intent = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_INICIAR
            putExtra(ContadorService.EXTRA_SEGUNDOS, totalSegundos)
        }
        contexto.startForegroundService(intent)
    }

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

    fun iniciarContador(totalSegundos: Int) {
        mostrarContador = true
        corriendo = true
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
            corriendo = false
        }
    }

    fun iniciar() {
        val partes = tiempoFormato.split(":")
        val restantes = partes[0].toInt() * 3600 + partes[1].toInt() * 60 + partes[2].toInt()
        if (restantes <= 0) return

        corriendo = true
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var r = restantes
            while (r >= 0) {
                val h = r / 3600
                val m = (r % 3600) / 60
                val s = r % 60
                tiempoFormato = String.format("%02d:%02d:%02d", h, m, s)
                if (r == 0) break
                delay(1000L)
                r--
            }
            corriendo = false
        }
    }

    fun parar(contexto: Context) {
        corriendo = false
        countdownJob?.cancel()
        val intent = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_PARAR
        }
        contexto.startService(intent)
    }

    fun cancelar(contexto: Context) {
        corriendo = false
        mostrarContador = false
        countdownJob?.cancel()
        tiempoFormato = "00:00:00"
        val intent = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_CANCELAR
        }
        contexto.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
    fun comprobarEstadoService() {
        viewModelScope.launch {
            while (true) {

                if (!ContadorService.estaActivo && !ContadorService.estaPausado) {
                    mostrarContador = false
                    corriendo = false
                    tiempoFormato = "00:00:00"
                    countdownJob?.cancel()
                }

                delay(300)
            }
        }
    }
}