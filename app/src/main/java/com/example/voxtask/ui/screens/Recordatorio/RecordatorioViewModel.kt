package com.example.voxtask.ui.screens.Recordatorio

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Evento
import com.example.voxtask.databases.repository.EventoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecordatorioViewModel : ViewModel() {

    //Variables
    private val repository = EventoRepository()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var diaSeleccionado by mutableStateOf<LocalDate?>(null)
        private set
    val eventos = mutableStateListOf<Evento>()
    private var esperandoAsunto = false
    var onHablar: ((String) -> Unit)? = null

    init {
        cargarEventos()
    }

    //Funcion que sirve para cargar los eventos del usuario logueado
    private fun cargarEventos() {
        viewModelScope.launch {
            val listaEventos = repository.obtenerTodos(usuarioId)
            eventos.clear()
            eventos.addAll(listaEventos)
        }
    }

    //Funcion que actualiza el dia seleccionado al pulsar en el calendario
    fun seleccionarDia(fecha: LocalDate) {
        diaSeleccionado = fecha
    }
    //Funcion que recibe el texto transformado por voz y lo convierte a minusculas y elimina espacios
    @RequiresApi(Build.VERSION_CODES.O)
    fun onTextoRecibido(texto: String) {
        val textoLimpio = texto.lowercase().trim()

        Log.d("Recordatorio", "Texto recibido: $textoLimpio")
        Log.d("Recordatorio", "esperandoAsunto: $esperandoAsunto")
        Log.d("Recordatorio", "diaSeleccionado: $diaSeleccionado")

        // Si estamos esperando el asunto, guardamos el evento
        if (esperandoAsunto) {
            guardarEvento(textoLimpio)
            return
        }

        procesarComando(textoLimpio)
    }

    //Funcion para procesar el texto mediante la voz y ejecutar las acciones programadas
    @RequiresApi(Build.VERSION_CODES.O)
    private fun procesarComando(texto: String) {
        when {
            texto.contains("crea evento") ||
                    texto.contains("añade evento") ||
                    texto.contains("nuevo evento") -> {
                val fecha = extraerFecha(texto)
                if (fecha != null) {
                    diaSeleccionado = fecha
                    esperandoAsunto = true
                    onHablar?.invoke("¿Qué quieres añadir como asunto?")
                } else {
                }
            }

            texto.startsWith("elimina") ||
                    texto.startsWith("quita") ||
                    texto.startsWith("borra") -> {
                val asunto = texto
                    .removePrefix("elimina")
                    .removePrefix("quita")
                    .removePrefix("borra")
                    .trim()

                val evento = eventos.find { it.asunto.equals(asunto, ignoreCase = true) }
                if (evento != null) {
                    eliminarEvento(evento)
                } else {
                    onHablar?.invoke("No encontré ningún evento llamado $asunto")
                }
            }
        }
    }

    //Funcion para extraer la fecha del texto reconocido por voz y la convierte a un localDate
    @RequiresApi(Build.VERSION_CODES.O)
    private fun extraerFecha(texto: String): LocalDate? {
        val meses = mapOf(
            "enero" to 1, "febrero" to 2, "marzo" to 3,
            "abril" to 4, "mayo" to 5, "junio" to 6,
            "julio" to 7, "agosto" to 8, "septiembre" to 9,
            "octubre" to 10, "noviembre" to 11, "diciembre" to 12
        )

        return try {
            // Buscamos el número del día
            val dia = Regex("día (\\d{1,2})").find(texto)?.groupValues?.get(1)?.toInt()
                ?: Regex("(\\d{1,2}) de").find(texto)?.groupValues?.get(1)?.toInt()
                ?: return null

            // Buscamos el mes
            val mes = meses.entries.firstOrNull { texto.contains(it.key) }?.value
                ?: return null

            // Buscamos el año
            val anio = Regex("(\\d{4})").find(texto)?.groupValues?.get(1)?.toInt()
                ?: LocalDate.now().year

            //Lo convertimos a LocalDate
            LocalDate.of(anio, mes, dia)
        } catch (e: Exception) {
            null
        }
    }

    //Funcion para guardar un evento del calendario
    @RequiresApi(Build.VERSION_CODES.O)
    private fun guardarEvento(asunto: String) {
        val dia = diaSeleccionado ?: return

        val evento = Evento(
            asunto = asunto,
            dia = dia.dayOfMonth,
            mes = dia.monthValue,
            anio = dia.year
        )

        viewModelScope.launch {
            repository.agregar(usuarioId, evento)
            eventos.add(evento)
            onHablar?.invoke("Evento $asunto guardado para el ${dia.dayOfMonth} de ${dia.month}")

            esperandoAsunto = false
            diaSeleccionado = null
        }
    }

    //Funcion para eliminar un evento del calendario
    fun eliminarEvento(evento: Evento) {
        viewModelScope.launch {
            repository.eliminar(usuarioId, evento.id)
            eventos.remove(evento)
            onHablar?.invoke("Evento ${evento.asunto} eliminado")
        }
    }
}