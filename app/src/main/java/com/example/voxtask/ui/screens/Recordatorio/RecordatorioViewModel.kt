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
import com.example.voxtask.databases.repository.EventoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.voxtask.databases.model.Evento


// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
class RecordatorioViewModel : ViewModel() {

    // ── Estado observable ─────────────────────────────────────────────────────

    val eventos = mutableStateListOf<Evento>()  // Ahora usa el modelo de BD
    private val repository = EventoRepository()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var cargando: Boolean by mutableStateOf(false)
        private set

    init {
        cargarEventos()  // 👈 Carga al arrancar el ViewModel
    }

    var diaSeleccionado: LocalDate? by mutableStateOf(null)
        private set

    /** True mientras el flujo de creación de evento por voz está activo */
    var creandoEvento: Boolean by mutableStateOf(false)
        private set

    /** Callback para hablar (se inyecta desde la Screen) */
    var onHablar: ((String) -> Unit)? = null

    // ── Estado interno del flujo de voz ───────────────────────────────────────

    private enum class FlujoVoz {
        NINGUNO,
        // Flujo CREAR - pasos
        CREAR_ESPERANDO_DIA,
        CREAR_ESPERANDO_MES,
        CREAR_ESPERANDO_ANIO,
        CREAR_ESPERANDO_ASUNTO,
        // Flujo ELIMINAR
        ELIMINAR_ESPERANDO_DIA,
        ELIMINAR_ESPERANDO_MES,
        ELIMINAR_ESPERANDO_ANIO
    }

    private var flujoActual = FlujoVoz.NINGUNO

    /** Partes de fecha que vamos acumulando paso a paso */
    private var diaPendiente: Int? = null
    private var mesPendiente: Int? = null
    private var anioPendiente: Int? = null

    // ──────────────────────────────────────────────────────────────────────────
    // API pública
    // ──────────────────────────────────────────────────────────────────────────

    fun seleccionarDia(fecha: LocalDate) {
        diaSeleccionado = fecha
    }

    fun eliminarEvento(evento: Evento) {
        viewModelScope.launch {
            try {
                repository.eliminar(usuarioId, evento.id)  // ✅ id existe
                eventos.remove(evento)
            } catch (e: Exception) {
                Log.e("VoxTask", "Error al eliminar: ${e.message}")
            }
        }
    }
    fun cargarEventos() {
        viewModelScope.launch {
            cargando = true
            try {
                val lista = repository.obtenerTodos(usuarioId)
                eventos.clear()
                eventos.addAll(lista)
            } catch (e: Exception) {
                Log.e("VoxTask", "Error al cargar eventos: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }
    /** Punto de entrada para todo texto reconocido por voz */
    fun onTextoRecibido(texto: String) {
        Log.d("VoxTask", "onTextoRecibido → texto='$texto' | flujo=$flujoActual")
        val textoNorm = texto.trim().lowercase()

        // Cancelación global en cualquier momento
        if (esCancelacion(textoNorm) && flujoActual != FlujoVoz.NINGUNO) {
            resetFlujo()
            hablar(mensajeCancelado())
            return
        }

        when (flujoActual) {
            FlujoVoz.NINGUNO                  -> procesarComandoInicial(textoNorm)
            FlujoVoz.CREAR_ESPERANDO_DIA      -> procesarDia(textoNorm, esCrear = true)
            FlujoVoz.CREAR_ESPERANDO_MES      -> procesarMes(textoNorm, esCrear = true)
            FlujoVoz.CREAR_ESPERANDO_ANIO     -> procesarAnio(textoNorm, esCrear = true)
            FlujoVoz.CREAR_ESPERANDO_ASUNTO   -> procesarAsunto(textoNorm)
            FlujoVoz.ELIMINAR_ESPERANDO_DIA   -> procesarDia(textoNorm, esCrear = false)
            FlujoVoz.ELIMINAR_ESPERANDO_MES   -> procesarMes(textoNorm, esCrear = false)
            FlujoVoz.ELIMINAR_ESPERANDO_ANIO  -> procesarAnio(textoNorm, esCrear = false)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Procesamiento del comando inicial
    // ──────────────────────────────────────────────────────────────────────────

    private fun procesarComandoInicial(texto: String) {
        Log.d("VoxTask", "procesarComandoInicial → texto='$texto'")
        Log.d("VoxTask", "  esCrear=${esComandoCrear(texto)} | esEliminar=${esComandoEliminar(texto)}")

        when {
            esComandoCrear(texto)    -> iniciarCreacion()
            esComandoEliminar(texto) -> iniciarEliminacion()
        }
    }

    private fun iniciarCreacion() {
        creandoEvento = true
        flujoActual = FlujoVoz.CREAR_ESPERANDO_DIA
        hablar(mensajePreguntaDia())
    }

    private fun iniciarEliminacion() {
        flujoActual = FlujoVoz.ELIMINAR_ESPERANDO_DIA
        hablar(mensajePreguntaDia())
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Pasos del flujo guiado
    // ──────────────────────────────────────────────────────────────────────────

    /** Paso 1: esperar el número de día */
    private fun procesarDia(texto: String, esCrear: Boolean) {
        val dia = extraerNumero(texto)
        Log.d("VoxTask", "procesarDia → texto='$texto' | extraído=$dia")

        if (dia == null || dia < 1 || dia > 31) {
            hablar(mensajeNoEntendiDia())
            return
        }
        diaPendiente = dia
        flujoActual = if (esCrear) FlujoVoz.CREAR_ESPERANDO_MES else FlujoVoz.ELIMINAR_ESPERANDO_MES
        hablar(mensajePreguntaMes())
    }

    /** Paso 2: esperar el nombre o número del mes */
    private fun procesarMes(texto: String, esCrear: Boolean) {
        // Intentar por nombre primero, luego por número
        val mes = nombreMesANumero(texto) ?: extraerNumero(texto)
        if (mes == null || mes < 1 || mes > 12) {
            hablar(mensajeNoEntendiMes())
            return
        }
        mesPendiente = mes
        flujoActual = if (esCrear) FlujoVoz.CREAR_ESPERANDO_ANIO else FlujoVoz.ELIMINAR_ESPERANDO_ANIO
        hablar(mensajePreguntaAnio())
    }

    /** Paso 3: esperar si es este año u otro */
    private fun procesarAnio(texto: String, esCrear: Boolean) {
        val anioActual = LocalDate.now().year
        val anio: Int? = when {
            // "este año", "this year", "cette année", "dieses jahr", "quest'anno", "este ano"
            esEsteAnio(texto) -> anioActual
            // Número explícito de 4 dígitos
            else -> extraerAnio(texto)
        }

        if (anio == null) {
            hablar(mensajeNoEntendiAnio())
            return
        }

        anioPendiente = anio

        // Validar que la fecha sea coherente
        val fecha = runCatching {
            LocalDate.of(anio, mesPendiente!!, diaPendiente!!)
        }.getOrNull()

        if (fecha == null) {
            hablar(mensajeFechaInvalida())
            resetFlujo()
            return
        }

        seleccionarDia(fecha)

        if (esCrear) {
            flujoActual = FlujoVoz.CREAR_ESPERANDO_ASUNTO
            hablar(mensajePreguntaAsunto(fecha))
        } else {
            // Flujo eliminar: buscar y borrar
            procesarEliminacionConFecha(fecha)
        }
    }

    /** Paso 4 (solo crear): recibir el asunto */
    private fun procesarAsunto(asunto: String) {
        if (asunto.isBlank()) { hablar(mensajeNoEntendiAsunto()); return }

        val dia  = diaPendiente  ?: run { resetFlujo(); return }
        val mes  = mesPendiente  ?: run { resetFlujo(); return }
        val anio = anioPendiente ?: run { resetFlujo(); return }

        val asuntoNorm = asunto.trim().lowercase()
        val duplicado = eventos.any {
            it.dia == dia && it.mes == mes && it.anio == anio &&
                    it.asunto.trim().lowercase() == asuntoNorm
        }

        if (duplicado) {
            hablar(mensajeAsuntoDuplicado(asunto))
            return
        }

        val evento = Evento(asunto = asunto, dia = dia, mes = mes, anio = anio)

        viewModelScope.launch {
            try {
                repository.agregar(usuarioId, evento)
                cargarEventos()
                hablar(mensajeEventoCreado(evento))
            } catch (e: Exception) {
                Log.e("VoxTask", "Error al guardar: ${e.message}")
            }
        }
        resetFlujo()
    }
    // ──────────────────────────────────────────────────────────────────────────
    // Flujo ELIMINAR con fecha ya construida
    // ──────────────────────────────────────────────────────────────────────────

    private fun procesarEliminacionConFecha(fecha: LocalDate) {
        val eventosDia = eventos.filter {
            it.dia == fecha.dayOfMonth &&
                    it.mes == fecha.monthValue &&
                    it.anio == fecha.year
        }

        resetFlujo()

        if (eventosDia.isEmpty()) {
            hablar(mensajeSinEventos(fecha))
            return
        }

        viewModelScope.launch {
            try {
                eventosDia.forEach { evento ->
                    repository.eliminar(usuarioId, evento.id)  // ✅ id existe en el modelo de BD
                }
                cargarEventos()

                if (eventosDia.size == 1)
                    hablar(mensajeEventoEliminado(eventosDia.first()))
                else
                    hablar(mensajeTodosEliminados(fecha, eventosDia.size))

            } catch (e: Exception) {
                Log.e("VoxTask", "Error al eliminar: ${e.message}")
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Detección de intención
    // ──────────────────────────────────────────────────────────────────────────

    private fun esComandoCrear(texto: String): Boolean {
        val palabras = listOf(
            "añade evento", "añadir evento", "crea evento", "crear evento",
            "nuevo evento", "agregar evento", "agrega evento",
            "add event", "create event", "new event",
            "ajouter événement", "créer événement", "nouvel événement",
            "ajouter evenement", "creer evenement",
            "ereignis erstellen", "ereignis hinzufügen", "neues ereignis",
            "event erstellen", "event hinzufügen",
            "aggiungi evento", "nuovo evento",
            "adicionar evento", "criar evento", "novo evento"
        )
        return palabras.any { texto.contains(it) }
    }

    private fun esComandoEliminar(texto: String): Boolean {
        val palabras = listOf(
            "elimina evento", "eliminar evento", "borra evento", "borrar evento",
            "quita evento", "quitar evento",
            "delete event", "remove event", "cancel event",
            "supprimer événement", "effacer événement", "supprimer evenement",
            "ereignis löschen", "ereignis entfernen", "event löschen",
            "rimuovi evento", "cancella evento",
            "remover evento", "apagar evento"
        )
        return palabras.any { texto.contains(it) }
    }

    private fun esCancelacion(texto: String): Boolean {
        val palabras = listOf(
            "cancelar", "cancel", "annuler", "abbrechen", "annulla", "cancelar", "parar", "stop"
        )
        return palabras.any { texto.contains(it) }
    }

    private fun esEsteAnio(texto: String): Boolean {
        val palabras = listOf(
            // ES
            "este año", "este ano",
            // EN
            "this year", "current year",
            // FR
            "cette année", "cette annee",
            // DE
            "dieses jahr",
            // IT
            "quest'anno", "questo anno",
            // PT
            "este ano"
        )
        return palabras.any { texto.contains(it) }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Extracción de valores numéricos
    // ──────────────────────────────────────────────────────────────────────────

    /** Extrae el primer número entero encontrado en el texto */
    /** Extrae un año de 4 dígitos (entre 2000 y 2100) */
    private fun extraerAnio(texto: String): Int? {
        return Regex("""\b(20\d{2}|21\d{2})\b""").find(texto)?.value?.toIntOrNull()
    }

    /** Mapeo de nombres de mes en 6 idiomas → número de mes */
    private fun nombreMesANumero(texto: String): Int? {
        // Normalizar acentos para comparación
        val n = texto.lowercase()
            .replace("á", "a").replace("é", "e")
            .replace("í", "i").replace("ó", "o")
            .replace("ú", "u").replace("ü", "u")
            .replace("è", "e").replace("ê", "e")
            .replace("â", "a").replace("ô", "o")
            .replace("î", "i")

        // Mapa completo: todas las palabras clave de mes en 6 idiomas
        val mapaAbrevMes = mapOf(
            // Enero / January / Janvier / Januar / Gennaio / Janeiro
            "enero" to 1, "ene" to 1,
            "january" to 1, "jan" to 1,
            "janvier" to 1,
            "januar" to 1,
            "gennaio" to 1, "gen" to 1,
            "janeiro" to 1,
            // Febrero / February / Février / Februar / Febbraio / Fevereiro
            "febrero" to 2, "feb" to 2,
            "february" to 2,
            "fevrier" to 2, "fev" to 2,
            "februar" to 2,
            "febbraio" to 2,
            "fevereiro" to 2,
            // Marzo / March / Mars / März / Marzo / Março
            "marzo" to 3, "mar" to 3,
            "march" to 3,
            "mars" to 3,
            "marz" to 3,
            "marco" to 3,
            // Abril / April / Avril / April / Aprile / Abril
            "abril" to 4, "abr" to 4,
            "april" to 4, "apr" to 4,
            "avril" to 4,
            "aprile" to 4,
            // Mayo / May / Mai / Mai / Maggio / Maio
            "mayo" to 5,
            "may" to 5,
            "mai" to 5,
            "maggio" to 5, "mag" to 5,
            "maio" to 5,
            // Junio / June / Juin / Juni / Giugno / Junho
            "junio" to 6, "jun" to 6,
            "june" to 6,
            "juin" to 6,
            "juni" to 6,
            "giugno" to 6, "giu" to 6,
            "junho" to 6,
            // Julio / July / Juillet / Juli / Luglio / Julho
            "julio" to 7, "jul" to 7,
            "july" to 7,
            "juillet" to 7,
            "juli" to 7,
            "luglio" to 7, "lug" to 7,
            "julho" to 7,
            // Agosto / August / Août / August / Agosto / Agosto
            "agosto" to 8, "ago" to 8,
            "august" to 8, "aug" to 8,
            "aout" to 8,
            // Septiembre / September / Septembre / September / Settembre / Setembro
            "septiembre" to 9, "sep" to 9, "sept" to 9,
            "september" to 9,
            "septembre" to 9,
            "settembre" to 9, "set" to 9,
            "setembro" to 9,
            // Octubre / October / Octobre / Oktober / Ottobre / Outubro
            "octubre" to 10, "oct" to 10,
            "october" to 10,
            "octobre" to 10,
            "oktober" to 10, "okt" to 10,
            "ottobre" to 10, "ott" to 10,
            "outubro" to 10,
            // Noviembre / November / Novembre / November / Novembre / Novembro
            "noviembre" to 11, "nov" to 11,
            "november" to 11,
            "novembre" to 11,
            "novembro" to 11,
            // Diciembre / December / Décembre / Dezember / Dicembre / Dezembro
            "diciembre" to 12, "dic" to 12,
            "december" to 12, "dec" to 12,
            "decembre" to 12,
            "dezember" to 12, "dez" to 12,
            "dicembre" to 12,
            "dezembro" to 12
        )

        // Buscar coincidencia exacta de palabra completa en el texto normalizado
        val palabras = n.split(Regex("""\s+"""))
        for (palabra in palabras) {
            mapaAbrevMes[palabra]?.let { return it }
        }
        // También buscar si el texto completo normalizado contiene el nombre
        for ((nombre, numero) in mapaAbrevMes) {
            if (n.contains(nombre)) return numero
        }
        return null
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mensajes TTS multiidioma
    // ──────────────────────────────────────────────────────────────────────────

    private fun idioma(): String = try {
        com.example.voxtask.utils.TextoAVoz.localeActual.language
    } catch (e: Exception) { "es" }

    private fun mensajePreguntaDia(): String = when (idioma()) {
        "en" -> "What day? Say a number."
        "fr" -> "Quel jour ? Dites un numéro."
        "de" -> "Welcher Tag? Sagen Sie eine Zahl."
        "it" -> "Che giorno? Dì un numero."
        "pt" -> "Que dia? Diga um número."
        else -> "¿Qué día? Di un número."
    }

    private fun mensajePreguntaMes(): String = when (idioma()) {
        "en" -> "What month?"
        "fr" -> "Quel mois ?"
        "de" -> "Welcher Monat?"
        "it" -> "Che mese?"
        "pt" -> "Que mês?"
        else -> "¿Qué mes?"
    }

    private fun mensajePreguntaAnio(): String = when (idioma()) {
        "en" -> "This year, or which year?"
        "fr" -> "Cette année, ou quelle année ?"
        "de" -> "Dieses Jahr oder welches Jahr?"
        "it" -> "Quest'anno o quale anno?"
        "pt" -> "Este ano ou qual ano?"
        else -> "¿Este año o qué año?"
    }

    private fun mensajePreguntaAsunto(fecha: LocalDate): String {
        val d = fecha.dayOfMonth; val m = fecha.monthValue; val a = fecha.year
        return when (idioma()) {
            "en" -> "Date: $d/$m/$a. What subject do you want to add?"
            "fr" -> "Date : $d/$m/$a. Quel sujet voulez-vous ajouter ?"
            "de" -> "Datum: $d.$m.$a. Welchen Betreff möchten Sie hinzufügen?"
            "it" -> "Data: $d/$m/$a. Che oggetto vuoi aggiungere?"
            "pt" -> "Data: $d/$m/$a. Qual assunto deseja adicionar?"
            else -> "Fecha: $d/$m/$a. ¿Qué asunto quieres ponerle?"
        }
    }

    private fun mensajeEventoCreado(evento: Evento): String {
        val d = evento.dia; val m = evento.mes; val a = evento.anio
        return when (idioma()) {
            "en" -> "Event '${evento.asunto}' created for $d/$m/$a."
            "fr" -> "Événement '${evento.asunto}' créé pour le $d/$m/$a."
            "de" -> "Ereignis '${evento.asunto}' für den $d.$m.$a erstellt."
            "it" -> "Evento '${evento.asunto}' creato per il $d/$m/$a."
            "pt" -> "Evento '${evento.asunto}' criado para $d/$m/$a."
            else -> "Evento '${evento.asunto}' creado para el $d/$m/$a."
        }
    }

    private fun mensajeEventoEliminado(evento: Evento): String {
        val d = evento.dia; val m = evento.mes; val a = evento.anio
        return when (idioma()) {
            "en" -> "Event '${evento.asunto}' on $d/$m/$a deleted."
            "fr" -> "Événement '${evento.asunto}' du $d/$m/$a supprimé."
            "de" -> "Ereignis '${evento.asunto}' vom $d.$m.$a gelöscht."
            "it" -> "Evento '${evento.asunto}' del $d/$m/$a eliminato."
            "pt" -> "Evento '${evento.asunto}' de $d/$m/$a removido."
            else -> "Evento '${evento.asunto}' del $d/$m/$a eliminado."
        }
    }
    private fun mensajeTodosEliminados(fecha: LocalDate, cantidad: Int): String {
        val d = fecha.dayOfMonth; val m = fecha.monthValue; val a = fecha.year
        return when (idioma()) {
            "en" -> "$cantidad events on $d/$m/$a have been deleted."
            "fr" -> "$cantidad événements du $d/$m/$a ont été supprimés."
            "de" -> "$cantidad Ereignisse vom $d.$m.$a wurden gelöscht."
            "it" -> "$cantidad eventi del $d/$m/$a sono stati eliminati."
            "pt" -> "$cantidad eventos de $d/$m/$a foram removidos."
            else -> "Se han eliminado $cantidad eventos del $d/$m/$a."
        }
    }

    private fun mensajeSinEventos(fecha: LocalDate): String {
        val d = fecha.dayOfMonth; val m = fecha.monthValue; val a = fecha.year
        return when (idioma()) {
            "en" -> "No events found for $d/$m/$a."
            "fr" -> "Aucun événement trouvé pour le $d/$m/$a."
            "de" -> "Keine Ereignisse für den $d.$m.$a gefunden."
            "it" -> "Nessun evento trovato per il $d/$m/$a."
            "pt" -> "Nenhum evento encontrado para $d/$m/$a."
            else -> "No se encontraron eventos para el $d/$m/$a."
        }
    }

    private fun mensajeNoEntendiDia(): String = when (idioma()) {
        "en" -> "I didn't understand the day. Please say a number between 1 and 31."
        "fr" -> "Je n'ai pas compris le jour. Dites un numéro entre 1 et 31."
        "de" -> "Ich habe den Tag nicht verstanden. Bitte sagen Sie eine Zahl zwischen 1 und 31."
        "it" -> "Non ho capito il giorno. Di' un numero tra 1 e 31."
        "pt" -> "Não entendi o dia. Diga um número entre 1 e 31."
        else -> "No entendí el día. Di un número entre 1 y 31."
    }

    /** Extrae el primer número entero encontrado en el texto.
     *  Acepta tanto cifras ("8") como palabras numéricas en 6 idiomas
     *  ("eight", "huit", "acht", "otto", "oito", "ocho"). */
    private fun extraerNumero(texto: String): Int? {
        // ── 1. Normalizar acentos ──────────────────────────────────────────────
        val norm = texto.lowercase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ü","u")
            .replace("è","e").replace("ê","e").replace("â","a")
            .replace("ô","o").replace("î","i").replace("ñ","n")

        // ── 2. Mapa de palabras → número (ES / EN / FR / DE / IT / PT) ─────────
        val palabrasNumericas = mapOf(
            // 0
            "cero" to 0, "zero" to 0, "zéro" to 0, "null" to 0,
            // 1
            "uno" to 1, "un" to 1, "uma" to 1, "one" to 1,
            "un" to 1, "une" to 1, "ein" to 1, "eins" to 1,
            "uno" to 1, "um" to 1,
            // 2
            "dos" to 2, "two" to 2, "deux" to 2, "zwei" to 2,
            "due" to 2, "dois" to 2,
            // 3
            "tres" to 3, "three" to 3, "trois" to 3, "drei" to 3,
            "tre" to 3,
            // 4
            "cuatro" to 4, "four" to 4, "quatre" to 4, "vier" to 4,
            "quattro" to 4, "quatro" to 4,
            // 5
            "cinco" to 5, "five" to 5, "cinq" to 5, "funf" to 5,
            "cinque" to 5,
            // 6
            "seis" to 6, "six" to 6, "sechs" to 6, "sei" to 6,
            // 7
            "siete" to 7, "seven" to 7, "sept" to 7, "sieben" to 7,
            "sette" to 7, "sete" to 7,
            // 8
            "ocho" to 8, "eight" to 8, "huit" to 8, "acht" to 8,
            "otto" to 8, "oito" to 8,
            // 9
            "nueve" to 9, "nine" to 9, "neuf" to 9, "neun" to 9,
            "nove" to 9,
            // 10
            "diez" to 10, "ten" to 10, "dix" to 10, "zehn" to 10,
            "dieci" to 10, "dez" to 10,
            // 11
            "once" to 11, "eleven" to 11, "onze" to 11, "elf" to 11,
            "undici" to 11, "onze" to 11,
            // 12
            "doce" to 12, "twelve" to 12, "douze" to 12, "zwolf" to 12,
            "dodici" to 12, "doze" to 12,
            // 13
            "trece" to 13, "thirteen" to 13, "treize" to 13,
            "dreizehn" to 13, "tredici" to 13, "treze" to 13,
            // 14
            "catorce" to 14, "fourteen" to 14, "quatorze" to 14,
            "vierzehn" to 14, "quattordici" to 14, "quatorze" to 14,
            // 15
            "quince" to 15, "fifteen" to 15, "quinze" to 15,
            "funfzehn" to 15, "quindici" to 15,
            // 16
            "dieciseis" to 16, "sixteen" to 16, "seize" to 16,
            "sechzehn" to 16, "sedici" to 16, "dezasseis" to 16,
            // 17
            "diecisiete" to 17, "seventeen" to 17, "dix-sept" to 17,
            "siebzehn" to 17, "diciassette" to 17, "dezassete" to 17,
            // 18
            "dieciocho" to 18, "eighteen" to 18, "dix-huit" to 18,
            "achtzehn" to 18, "diciotto" to 18, "dezoito" to 18,
            // 19
            "diecinueve" to 19, "nineteen" to 19, "dix-neuf" to 19,
            "neunzehn" to 19, "diciannove" to 19, "dezanove" to 19,
            // 20
            "veinte" to 20, "twenty" to 20, "vingt" to 20,
            "zwanzig" to 20, "venti" to 20, "vinte" to 20,
            // 21-31 (días de calendario más usados por voz)
            "veintiuno" to 21, "twenty one" to 21, "twenty-one" to 21,
            "vingt et un" to 21, "einundzwanzig" to 21,
            "ventuno" to 21, "vinte e um" to 21,
            "veintidos" to 22, "twenty two" to 22, "twenty-two" to 22,
            "vingt-deux" to 22, "zweiundzwanzig" to 22,
            "ventidue" to 22, "vinte e dois" to 22,
            "veintitres" to 23, "twenty three" to 23, "twenty-three" to 23,
            "vingt-trois" to 23, "dreiundzwanzig" to 23,
            "ventitré" to 23, "vinte e tres" to 23,
            "veinticuatro" to 24, "twenty four" to 24, "twenty-four" to 24,
            "vingt-quatre" to 24, "vierundzwanzig" to 24,
            "ventiquattro" to 24, "vinte e quatro" to 24,
            "veinticinco" to 25, "twenty five" to 25, "twenty-five" to 25,
            "vingt-cinq" to 25, "funfundzwanzig" to 25,
            "venticinque" to 25, "vinte e cinco" to 25,
            "veintiseis" to 26, "twenty six" to 26, "twenty-six" to 26,
            "vingt-six" to 26, "sechsundzwanzig" to 26,
            "ventisei" to 26, "vinte e seis" to 26,
            "veintisiete" to 27, "twenty seven" to 27, "twenty-seven" to 27,
            "vingt-sept" to 27, "siebenundzwanzig" to 27,
            "ventisette" to 27, "vinte e sete" to 27,
            "veintiocho" to 28, "twenty eight" to 28, "twenty-eight" to 28,
            "vingt-huit" to 28, "achtundzwanzig" to 28,
            "ventotto" to 28, "vinte e oito" to 28,
            "veintinueve" to 29, "twenty nine" to 29, "twenty-nine" to 29,
            "vingt-neuf" to 29, "neunundzwanzig" to 29,
            "ventinove" to 29, "vinte e nove" to 29,
            "treinta" to 30, "thirty" to 30, "trente" to 30,
            "dreißig" to 30, "dreizig" to 30, "trenta" to 30, "trinta" to 30,
            "treinta y uno" to 31, "thirty one" to 31, "thirty-one" to 31,
            "trente et un" to 31, "einunddreißig" to 31,
            "trentuno" to 31, "trinta e um" to 31
        )

        // ── 3. Buscar frases compuestas primero (más específicas) ───────────────
        //       Ordenar por longitud descendente para que "twenty-one" gane a "one"
        val candidatoFrase = palabrasNumericas.entries
            .sortedByDescending { it.key.length }
            .firstOrNull { norm.contains(it.key) }
        if (candidatoFrase != null) return candidatoFrase.value

        // ── 4. Buscar palabra suelta con límites de palabra ─────────────────────
        val palabras = norm.split(Regex("""\s+"""))
        for (p in palabras) {
            palabrasNumericas[p]?.let { return it }
        }

        // ── 5. Fallback: primer dígito en el texto original ─────────────────────
        return Regex("""\d+""").find(texto)?.value?.toIntOrNull()
    }
    private fun mensajeNoEntendiMes(): String = when (idioma()) {
        "en" -> "I didn't understand the month. Please say the month name or a number."
        "fr" -> "Je n'ai pas compris le mois. Dites le nom du mois ou un numéro."
        "de" -> "Ich habe den Monat nicht verstanden. Bitte sagen Sie den Monatsnamen oder eine Zahl."
        "it" -> "Non ho capito il mese. Di' il nome del mese o un numero."
        "pt" -> "Não entendi o mês. Diga o nome do mês ou um número."
        else -> "No entendí el mes. Di el nombre del mes o un número."
    }

    private fun mensajeNoEntendiAnio(): String = when (idioma()) {
        "en" -> "I didn't understand the year. Say 'this year' or a four-digit year like 2026."
        "fr" -> "Je n'ai pas compris l'année. Dites 'cette année' ou une année comme 2026."
        "de" -> "Ich habe das Jahr nicht verstanden. Sagen Sie 'dieses Jahr' oder z.B. 2026."
        "it" -> "Non ho capito l'anno. Di' 'quest'anno' o un anno come 2026."
        "pt" -> "Não entendi o ano. Diga 'este ano' ou um ano como 2026."
        else -> "No entendí el año. Di 'este año' o un año como 2026."
    }

    private fun mensajeNoEntendiAsunto(): String = when (idioma()) {
        "en" -> "I didn't catch the subject. Please say it again."
        "fr" -> "Je n'ai pas saisi le sujet. Veuillez répéter."
        "de" -> "Ich habe den Betreff nicht verstanden. Bitte wiederholen."
        "it" -> "Non ho capito l'oggetto. Ripeti per favore."
        "pt" -> "Não entendi o assunto. Por favor repita."
        else -> "No entendí el asunto. Por favor repite."
    }

    private fun mensajeFechaInvalida(): String = when (idioma()) {
        "en" -> "That date is not valid. Please try again."
        "fr" -> "Cette date n'est pas valide. Veuillez réessayer."
        "de" -> "Dieses Datum ist ungültig. Bitte versuchen Sie es erneut."
        "it" -> "Quella data non è valida. Riprova per favore."
        "pt" -> "Essa data não é válida. Por favor tente novamente."
        else -> "Esa fecha no es válida. Por favor inténtalo de nuevo."
    }

    private fun mensajeCancelado(): String = when (idioma()) {
        "en" -> "Cancelled."
        "fr" -> "Annulé."
        "de" -> "Abgebrochen."
        "it" -> "Annullato."
        "pt" -> "Cancelado."
        else -> "Cancelado."
    }

    private fun mensajeAsuntoDuplicado(asunto: String): String = when (idioma()) {
        "en" -> "The event '$asunto' already exists on that date. Please say a different subject."
        "fr" -> "L'événement '$asunto' existe déjà à cette date. Veuillez dire un autre sujet."
        "de" -> "Das Ereignis '$asunto' existiert bereits an diesem Datum. Bitte sagen Sie einen anderen Betreff."
        "it" -> "L'evento '$asunto' esiste già in quella data. Per favore di' un oggetto diverso."
        "pt" -> "O evento '$asunto' já existe nessa data. Por favor diga um assunto diferente."
        else -> "Ya existe un evento '$asunto' en esa fecha. Di otro asunto para el evento."
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utilidades internas
    // ──────────────────────────────────────────────────────────────────────────

    private fun hablar(mensaje: String) {
        onHablar?.invoke(mensaje)
    }

    private fun resetFlujo() {
        flujoActual    = FlujoVoz.NINGUNO
        diaPendiente   = null
        mesPendiente   = null
        anioPendiente  = null
        creandoEvento  = false
    }
}