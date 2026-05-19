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
import com.example.voxtask.utils.TextoAVoz
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
        val idioma = TextoAVoz.localeActual.language

        val comandosCrear = when (idioma) {
            "en" -> listOf("create event", "add event", "new event")
            "fr" -> listOf("créer événement", "ajouter événement", "nouvel événement")
            "de" -> listOf("ereignis erstellen", "ereignis hinzufügen", "neues ereignis")
            "it" -> listOf("crea evento", "aggiungi evento", "nuovo evento")
            "pt" -> listOf("criar evento", "adicionar evento", "novo evento")
            else -> listOf("crea evento", "añade evento", "crear evento", "nuevo evento")
        }

        val prefijosEliminar = when (idioma) {
            "en" -> listOf("delete", "remove", "erase")
            "fr" -> listOf("supprime", "enlève", "efface")
            "de" -> listOf("lösche", "entferne", "streiche")
            "it" -> listOf("elimina", "rimuovi", "cancella")
            "pt" -> listOf("elimina", "remove", "apaga")
            else -> listOf("elimina", "quita", "borra")
        }

        val mensajeAsunto = when (idioma) {
            "en" -> "What do you want to add as the subject?"
            "fr" -> "Que voulez-vous ajouter comme sujet?"
            "de" -> "Was möchten Sie als Betreff hinzufügen?"
            "it" -> "Cosa vuoi aggiungere come oggetto?"
            "pt" -> "O que você quer adicionar como assunto?"
            else -> "¿Qué quieres añadir como asunto?"
        }

        val mensajeNoEncontrado = when (idioma) {
            "en" -> "I couldn't find any event called"
            "fr" -> "Je n'ai trouvé aucun événement appelé"
            "de" -> "Ich konnte kein Ereignis namens finden"
            "it" -> "Non ho trovato nessun evento chiamato"
            "pt" -> "Não encontrei nenhum evento chamado"
            else -> "No encontré ningún evento llamado"
        }

        when {
            comandosCrear.any { texto.contains(it) } -> {
                val fecha = extraerFecha(texto)
                if (fecha != null) {
                    diaSeleccionado = fecha
                    esperandoAsunto = true
                    onHablar?.invoke(mensajeAsunto)
                }
            }

            prefijosEliminar.any { texto.startsWith(it) } -> {
                val prefijoUsado = prefijosEliminar.first { texto.startsWith(it) }
                val asunto = texto.removePrefix(prefijoUsado).trim()
                val evento = eventos.find { it.asunto.equals(asunto, ignoreCase = true) }
                if (evento != null) {
                    eliminarEvento(evento)
                } else {
                    onHablar?.invoke("$mensajeNoEncontrado $asunto")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun extraerFecha(texto: String): LocalDate? {
        val idioma = TextoAVoz.localeActual.language

        val meses = when (idioma) {
            "en" -> mapOf(
                "january" to 1, "february" to 2, "march" to 3,
                "april" to 4, "may" to 5, "june" to 6,
                "july" to 7, "august" to 8, "september" to 9,
                "october" to 10, "november" to 11, "december" to 12
            )
            "fr" -> mapOf(
                "janvier" to 1, "février" to 2, "mars" to 3,
                "avril" to 4, "mai" to 5, "juin" to 6,
                "juillet" to 7, "août" to 8, "septembre" to 9,
                "octobre" to 10, "novembre" to 11, "décembre" to 12
            )
            "de" -> mapOf(
                "januar" to 1, "februar" to 2, "märz" to 3,
                "april" to 4, "mai" to 5, "juni" to 6,
                "juli" to 7, "august" to 8, "september" to 9,
                "oktober" to 10, "november" to 11, "dezember" to 12
            )
            "it" -> mapOf(
                "gennaio" to 1, "febbraio" to 2, "marzo" to 3,
                "aprile" to 4, "maggio" to 5, "giugno" to 6,
                "luglio" to 7, "agosto" to 8, "settembre" to 9,
                "ottobre" to 10, "novembre" to 11, "dicembre" to 12
            )
            "pt" -> mapOf(
                "janeiro" to 1, "fevereiro" to 2, "março" to 3,
                "abril" to 4, "maio" to 5, "junho" to 6,
                "julho" to 7, "agosto" to 8, "setembro" to 9,
                "outubro" to 10, "novembro" to 11, "dezembro" to 12
            )
            else -> mapOf(
                "enero" to 1, "febrero" to 2, "marzo" to 3,
                "abril" to 4, "mayo" to 5, "junio" to 6,
                "julio" to 7, "agosto" to 8, "septiembre" to 9,
                "octubre" to 10, "noviembre" to 11, "diciembre" to 12
            )
        }

        val partes = texto.split(" ")
        var dia: Int? = null
        var mes: Int? = null

        partes.forEachIndexed { index, parte ->
            // Buscar número del día
            val numero = parte.toIntOrNull()
            if (numero != null && numero in 1..31) {
                dia = numero
            }
            // Buscar nombre del mes
            meses[parte]?.let { mes = it }
        }

        return if (dia != null && mes != null) {
            try {
                LocalDate.of(LocalDate.now().year, mes!!, dia!!)
            } catch (e: Exception) {
                null
            }
        } else {
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