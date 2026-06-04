package com.example.voxtask.ui.screens.Contador

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.services.ContadorService
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContadorViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    /** Variables */
    private val _textoReconocido = MutableStateFlow("")
    var tiempoFormato   by mutableStateOf("00:00:00")
    var mostrarContador by mutableStateOf(false)
    var corriendo       by mutableStateOf(false)
        private set
    var terminado       by mutableStateOf(false)
        private set
    private var trabajoCuentaAtras: Job? = null
    private var reproductorAudio: android.media.MediaPlayer? = null

    /**
     * Permite recuperar el estado del contador desde el servicio al volver a la pantalla
     */
    fun restaurarSiServicioActivo() {
        if (ContadorService.segundosRestantes > 0) {
            mostrarContador = true
            if (ContadorService.estaActivo) {
                if (!corriendo) iniciarContador(ContadorService.segundosRestantes)
            } else if (ContadorService.estaPausado) {
                corriendo = false
                val horas   = ContadorService.segundosRestantes / 3600
                val minutos = (ContadorService.segundosRestantes % 3600) / 60
                val segs    = ContadorService.segundosRestantes % 60
                tiempoFormato = String.format("%02d:%02d:%02d", horas, minutos, segs)
            }
        }
    }

    /** Permite recibir lo que el usuario dice y procesarlo */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onTextoRecibido(texto: String, contexto: Context) {
        _textoReconocido.value = texto
        procesarComando(texto, contexto)
    }

    /**
     * Permite analizar el texto recibido, identificar los números y unidades de tiempo
     * en varios idiomas y calcula el total para iniciar el contador
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun procesarComando(texto: String, contexto: Context) {
        android.util.Log.d("CONTADOR", "Texto recibido: $texto")

        val textoNormalizado = normalizarNumeros(texto.lowercase().trim())
            .replace(" y ", " ")
        android.util.Log.d("CONTADOR", "Texto normalizado: $textoNormalizado")

        val partes = textoNormalizado.split("\\s+".toRegex())
        var totalSegundos = 0

        partes.forEachIndexed { indice, parte ->
            val numero = parte.toIntOrNull()
            if (numero != null) {
                val sig1   = partes.getOrNull(indice + 1)?.lowercase() ?: ""
                val sig2   = partes.getOrNull(indice + 2)?.lowercase() ?: ""
                val unidad = if (sig1.toIntOrNull() != null) sig2 else sig1

                when {
                    unidad.startsWith(when (TextoAVoz.localeActual.language) {
                        "en" -> "hour"; "fr" -> "heure"; "de" -> "stunde"
                        "it" -> "ora";  "pt" -> "hora";  else -> "hora"
                    }) -> totalSegundos += numero * 3600

                    unidad.startsWith("min") -> totalSegundos += numero * 60

                    unidad.startsWith(when (TextoAVoz.localeActual.language) {
                        "de" -> "sek"; "pt" -> "seg"; else -> "seg"
                    }) || unidad.startsWith("sec") -> totalSegundos += numero
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

    /**
     * Permite iniciar el servicio de contador en primer plano pasando los segundos calculados
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun iniciarContadorConServicio(contexto: Context, totalSegundos: Int) {
        val intento = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_INICIAR
            putExtra(ContadorService.EXTRA_SEGUNDOS, totalSegundos)
        }
        contexto.startForegroundService(intento)
    }

    /**
     * Ejecuta una alarma sonora y vibracion para avisar al usuario cuando finaliza el contador
     */
    private fun reproducirSonidoFin() {
        viewModelScope.launch(Dispatchers.Main) {
            val contexto = getApplication<Application>()

            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val reproductor = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(contexto, uri)
                    isLooping = false
                    prepare()
                }
                reproductor.setOnCompletionListener { it.release(); reproductorAudio = null }
                reproductor.start()
                reproductorAudio = reproductor
                android.util.Log.d("CONTADOR", "Sonido reproducido correctamente")
            } catch (e: Exception) {
                android.util.Log.e("CONTADOR", "Error al reproducir sonido: ${e.message}")
            }

            try {
                val patron = longArrayOf(0, 400, 200, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val gestorVibracion = contexto.getSystemService(VibratorManager::class.java)
                    gestorVibracion?.defaultVibrator?.vibrate(
                        VibrationEffect.createWaveform(patron, -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrador = contexto.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrador?.vibrate(VibrationEffect.createWaveform(patron, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrador?.vibrate(patron, -1)
                    }
                }
                android.util.Log.d("CONTADOR", "Vibración ejecutada correctamente")
            } catch (e: Exception) {
                android.util.Log.e("CONTADOR", "Error al vibrar: ${e.message}")
            }
        }
    }

    /**
     * Permite inicia la cuenta atrás visual, actualizando el formato
     * del tiempo cada segundo y gestionando el evento de finalización
     */
    fun iniciarContador(totalSegundos: Int) {
        mostrarContador = true
        corriendo       = true
        terminado       = false
        trabajoCuentaAtras?.cancel()
        trabajoCuentaAtras = viewModelScope.launch {
            var restantes = totalSegundos
            while (restantes >= 0) {
                withContext(Dispatchers.Main) {
                    val horas   = restantes / 3600
                    val minutos = (restantes % 3600) / 60
                    val segs    = restantes % 60
                    tiempoFormato = String.format("%02d:%02d:%02d", horas, minutos, segs)
                }
                if (restantes == 0) {
                    withContext(Dispatchers.Main) {
                        corriendo = false
                        terminado = true
                    }
                    reproducirSonidoFin()
                    break
                }
                delay(1000L)
                restantes--
            }
            withContext(Dispatchers.Main) { corriendo = false }
        }
    }

    /**
     * Permite reanudar la cuenta atrás sincronizando tanto la interfaz de usuario como el servicio en segundo plano
     */
    fun iniciar(contexto: Context) {
        val partes    = tiempoFormato.split(":")
        val restantes = partes[0].toInt() * 3600 + partes[1].toInt() * 60 + partes[2].toInt()
        if (restantes <= 0) return

        val intento = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_REANUDAR
        }
        contexto.startService(intento)

        corriendo = true
        terminado = false
        trabajoCuentaAtras?.cancel()
        trabajoCuentaAtras = viewModelScope.launch {
            var r = restantes
            while (r >= 0) {
                withContext(Dispatchers.Main) {
                    val horas   = r / 3600
                    val minutos = (r % 3600) / 60
                    val segs    = r % 60
                    tiempoFormato = String.format("%02d:%02d:%02d", horas, minutos, segs)
                }
                if (r == 0) {
                    withContext(Dispatchers.Main) {
                        corriendo = false
                        terminado = true
                    }
                    reproducirSonidoFin()
                    break
                }
                delay(1000L)
                r--
            }
            withContext(Dispatchers.Main) { corriendo = false }
        }
    }

    /**
     * Permite parar la cuenta atrás sincronizando tanto la interfaz de usuario como el servicio en segundo plano
     */
    fun parar(contexto: Context) {
        corriendo = false
        trabajoCuentaAtras?.cancel()
        val intento = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_PARAR
        }
        contexto.startService(intento)
    }

    /**
     * Permite cancelar la cuenta atrás sincronizando tanto la interfaz de usuario como el servicio en segundo plano
     */
    fun cancelar(contexto: Context) {
        corriendo       = false
        mostrarContador = false
        terminado       = false
        trabajoCuentaAtras?.cancel()
        tiempoFormato   = "00:00:00"

        reproductorAudio?.stop()
        reproductorAudio?.release()
        reproductorAudio = null

        val intento = Intent(contexto, ContadorService::class.java).apply {
            action = ContadorService.ACCION_CANCELAR
        }
        contexto.startService(intento)
    }

    /**
     * Permite cancelar cualquier tarea pendiente y liberar los recursos
     */
    override fun onCleared() {
        super.onCleared()
        trabajoCuentaAtras?.cancel()
        reproductorAudio?.release()
        reproductorAudio = null
    }

    /**
     * Permite mantener la interfaz de usuario sincronizada con el estado real del servicio
     */
    fun comprobarEstadoService() {
        viewModelScope.launch {
            while (true) {
                if (!terminado) {
                    when {
                        ContadorService.estaPausado && corriendo -> {
                            withContext(Dispatchers.Main) {
                                corriendo = false
                                trabajoCuentaAtras?.cancel()
                                val r       = ContadorService.segundosRestantes
                                val horas   = r / 3600
                                val minutos = (r % 3600) / 60
                                val segs    = r % 60
                                tiempoFormato = String.format("%02d:%02d:%02d", horas, minutos, segs)
                            }
                        }
                        ContadorService.estaActivo && !corriendo && mostrarContador -> {
                            iniciarContador(ContadorService.segundosRestantes)
                        }
                    }
                }
                delay(300)
            }
        }
    }

    /**
     * Permite traducir las palabras que representan números a su formato numérico según el idioma seleccionado.
     */
    private fun normalizarNumeros(
        texto: String,
        idioma: String = TextoAVoz.localeActual.language
    ): String {
        val reemplazos = linkedMapOf<String, String>()

        when (idioma) {
            "en" -> reemplazos.putAll(linkedMapOf(
                "fifty one" to "51", "fifty two" to "52", "fifty three" to "53",
                "fifty four" to "54", "fifty five" to "55", "fifty six" to "56",
                "fifty seven" to "57", "fifty eight" to "58", "fifty nine" to "59",
                "forty one" to "41", "forty two" to "42", "forty three" to "43",
                "forty four" to "44", "forty five" to "45", "forty six" to "46",
                "forty seven" to "47", "forty eight" to "48", "forty nine" to "49",
                "thirty one" to "31", "thirty two" to "32", "thirty three" to "33",
                "thirty four" to "34", "thirty five" to "35", "thirty six" to "36",
                "thirty seven" to "37", "thirty eight" to "38", "thirty nine" to "39",
                "twenty one" to "21", "twenty two" to "22", "twenty three" to "23",
                "twenty four" to "24", "twenty five" to "25", "twenty six" to "26",
                "twenty seven" to "27", "twenty eight" to "28", "twenty nine" to "29",
                "sixty" to "60", "fifty" to "50", "forty" to "40", "thirty" to "30",
                "twenty" to "20", "nineteen" to "19", "eighteen" to "18",
                "seventeen" to "17", "sixteen" to "16", "fifteen" to "15",
                "fourteen" to "14", "thirteen" to "13", "twelve" to "12",
                "eleven" to "11", "ten" to "10", "nine" to "9", "eight" to "8",
                "seven" to "7", "six" to "6", "five" to "5", "four" to "4",
                "three" to "3", "two" to "2", "one" to "1", "zero" to "0"
            ))
            "fr" -> reemplazos.putAll(linkedMapOf(
                "cinquante et un" to "51", "cinquante deux" to "52", "cinquante trois" to "53",
                "cinquante quatre" to "54", "cinquante cinq" to "55", "cinquante six" to "56",
                "cinquante sept" to "57", "cinquante huit" to "58", "cinquante neuf" to "59",
                "quarante et un" to "41", "quarante deux" to "42", "quarante trois" to "43",
                "quarante quatre" to "44", "quarante cinq" to "45", "quarante six" to "46",
                "quarante sept" to "47", "quarante huit" to "48", "quarante neuf" to "49",
                "trente et un" to "31", "trente deux" to "32", "trente trois" to "33",
                "trente quatre" to "34", "trente cinq" to "35", "trente six" to "36",
                "trente sept" to "37", "trente huit" to "38", "trente neuf" to "39",
                "vingt et un" to "21", "vingt deux" to "22", "vingt trois" to "23",
                "vingt quatre" to "24", "vingt cinq" to "25", "vingt six" to "26",
                "vingt sept" to "27", "vingt huit" to "28", "vingt neuf" to "29",
                "soixante" to "60", "cinquante" to "50", "quarante" to "40",
                "trente" to "30", "vingt" to "20", "dix-neuf" to "19", "dix neuf" to "19",
                "dix-huit" to "18", "dix huit" to "18", "dix-sept" to "17", "dix sept" to "17",
                "seize" to "16", "quinze" to "15", "quatorze" to "14", "treize" to "13",
                "douze" to "12", "onze" to "11", "dix" to "10", "neuf" to "9",
                "huit" to "8", "sept" to "7", "six" to "6", "cinq" to "5",
                "quatre" to "4", "trois" to "3", "deux" to "2", "un" to "1", "zéro" to "0"
            ))
            "de" -> reemplazos.putAll(linkedMapOf(
                "einundfünfzig" to "51", "zweiundfünfzig" to "52", "dreiundfünfzig" to "53",
                "vierundfünfzig" to "54", "fünfundfünfzig" to "55", "sechsundfünfzig" to "56",
                "siebenundfünfzig" to "57", "achtundfünfzig" to "58", "neunundfünfzig" to "59",
                "einundvierzig" to "41", "zweiundvierzig" to "42", "dreiundvierzig" to "43",
                "vierundvierzig" to "44", "fünfundvierzig" to "45", "sechsundvierzig" to "46",
                "siebenundvierzig" to "47", "achtundvierzig" to "48", "neunundvierzig" to "49",
                "einunddreißig" to "31", "zweiunddreißig" to "32", "dreiunddreißig" to "33",
                "vierunddreißig" to "34", "fünfunddreißig" to "35", "sechsunddreißig" to "36",
                "siebenunddreißig" to "37", "achtunddreißig" to "38", "neununddreißig" to "39",
                "einundzwanzig" to "21", "zweiundzwanzig" to "22", "dreiundzwanzig" to "23",
                "vierundzwanzig" to "24", "fünfundzwanzig" to "25", "sechsundzwanzig" to "26",
                "siebenundzwanzig" to "27", "achtundzwanzig" to "28", "neunundzwanzig" to "29",
                "sechzig" to "60", "fünfzig" to "50", "vierzig" to "40", "dreißig" to "30",
                "zwanzig" to "20", "neunzehn" to "19", "achtzehn" to "18", "siebzehn" to "17",
                "sechzehn" to "16", "fünfzehn" to "15", "vierzehn" to "14", "dreizehn" to "13",
                "zwölf" to "12", "elf" to "11", "zehn" to "10", "neun" to "9",
                "acht" to "8", "sieben" to "7", "sechs" to "6", "fünf" to "5",
                "vier" to "4", "drei" to "3", "zwei" to "2", "eins" to "1", "null" to "0"
            ))
            "it" -> reemplazos.putAll(linkedMapOf(
                "cinquantuno" to "51", "cinquantadue" to "52", "cinquantatré" to "53",
                "cinquantaquattro" to "54", "cinquantacinque" to "55", "cinquantasei" to "56",
                "cinquantasette" to "57", "cinquantotto" to "58", "cinquantanove" to "59",
                "quarantuno" to "41", "quarantadue" to "42", "quarantatré" to "43",
                "quarantaquattro" to "44", "quarantacinque" to "45", "quarantasei" to "46",
                "quarantasette" to "47", "quarantotto" to "48", "quarantanove" to "49",
                "trentuno" to "31", "trentadue" to "32", "trentatré" to "33",
                "trentaquattro" to "34", "trentacinque" to "35", "trentasei" to "36",
                "trentasette" to "37", "trentotto" to "38", "trentanove" to "39",
                "ventuno" to "21", "ventidue" to "22", "ventitré" to "23",
                "ventiquattro" to "24", "venticinque" to "25", "ventisei" to "26",
                "ventisette" to "27", "ventotto" to "28", "ventinove" to "29",
                "sessanta" to "60", "cinquanta" to "50", "quaranta" to "40",
                "trenta" to "30", "venti" to "20", "diciannove" to "19", "diciotto" to "18",
                "diciassette" to "17", "sedici" to "16", "quindici" to "15", "quattordici" to "14",
                "tredici" to "13", "dodici" to "12", "undici" to "11", "dieci" to "10",
                "nove" to "9", "otto" to "8", "sette" to "7", "sei" to "6",
                "cinque" to "5", "quattro" to "4", "tre" to "3", "due" to "2",
                "uno" to "1", "zero" to "0"
            ))
            "pt" -> reemplazos.putAll(linkedMapOf(
                "cinquenta e um" to "51", "cinquenta e dois" to "52", "cinquenta e três" to "53",
                "cinquenta e quatro" to "54", "cinquenta e cinco" to "55", "cinquenta e seis" to "56",
                "cinquenta e sete" to "57", "cinquenta e oito" to "58", "cinquenta e nove" to "59",
                "quarenta e um" to "41", "quarenta e dois" to "42", "quarenta e três" to "43",
                "quarenta e quatro" to "44", "quarenta e cinco" to "45", "quarenta e seis" to "46",
                "quarenta e sete" to "47", "quarenta e oito" to "48", "quarenta e nove" to "49",
                "trinta e um" to "31", "trinta e dois" to "32", "trinta e três" to "33",
                "trinta e quatro" to "34", "trinta e cinco" to "35", "trinta e seis" to "36",
                "trinta e sete" to "37", "trinta e oito" to "38", "trinta e nove" to "39",
                "vinte e um" to "21", "vinte e dois" to "22", "vinte e três" to "23",
                "vinte e quatro" to "24", "vinte e cinco" to "25", "vinte e seis" to "26",
                "vinte e sete" to "27", "vinte e oito" to "28", "vinte e nove" to "29",
                "sessenta" to "60", "cinquenta" to "50", "quarenta" to "40",
                "trinta" to "30", "vinte" to "20", "dezenove" to "19", "dezoito" to "18",
                "dezessete" to "17", "dezesseis" to "16", "quinze" to "15", "quatorze" to "14",
                "treze" to "13", "doze" to "12", "onze" to "11", "dez" to "10",
                "nove" to "9", "oito" to "8", "sete" to "7", "seis" to "6",
                "cinco" to "5", "quatro" to "4", "três" to "3", "dois" to "2",
                "um" to "1", "zero" to "0"
            ))
            else -> reemplazos.putAll(linkedMapOf(
                "cincuenta y uno" to "51", "cincuenta y dos" to "52", "cincuenta y tres" to "53",
                "cincuenta y cuatro" to "54", "cincuenta y cinco" to "55", "cincuenta y seis" to "56",
                "cincuenta y siete" to "57", "cincuenta y ocho" to "58", "cincuenta y nueve" to "59",
                "cuarenta y uno" to "41", "cuarenta y dos" to "42", "cuarenta y tres" to "43",
                "cuarenta y cuatro" to "44", "cuarenta y cinco" to "45", "cuarenta y seis" to "46",
                "cuarenta y siete" to "47", "cuarenta y ocho" to "48", "cuarenta y nueve" to "49",
                "treinta y uno" to "31", "treinta y dos" to "32", "treinta y tres" to "33",
                "treinta y cuatro" to "34", "treinta y cinco" to "35", "treinta y seis" to "36",
                "treinta y siete" to "37", "treinta y ocho" to "38", "treinta y nueve" to "39",
                "veinte y uno" to "21", "veintiuno" to "21", "veintidós" to "22", "veintidos" to "22",
                "veintitrés" to "23", "veintitres" to "23", "veinticuatro" to "24",
                "veinticinco" to "25", "veintiseis" to "26", "veintisiete" to "27",
                "veintiocho" to "28", "veintinueve" to "29",
                "sesenta" to "60", "cincuenta" to "50", "cuarenta" to "40",
                "treinta" to "30", "veinte" to "20", "diecinueve" to "19", "dieciocho" to "18",
                "diecisiete" to "17", "dieciséis" to "16", "dieciseis" to "16",
                "quince" to "15", "catorce" to "14", "trece" to "13", "doce" to "12",
                "once" to "11", "diez" to "10", "nueve" to "9", "ocho" to "8",
                "siete" to "7", "seis" to "6", "cinco" to "5", "cuatro" to "4",
                "tres" to "3", "dos" to "2", "uno" to "1", "una " to "1 ", "cero" to "0"
            ))
        }

        var resultado = texto
        reemplazos.forEach { (palabra, numero) ->
            resultado = resultado.replace(palabra, numero, ignoreCase = true)
        }
        return resultado
    }
}