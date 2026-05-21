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

    // ─── Variables ───────────────────────────────────────────────────────────
    private val repository = EventoRepository()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var diaSeleccionado by mutableStateOf<LocalDate?>(null)
        private set

    // Estado que indica si estamos en medio de la creación de un evento por voz
    // La UI lo usa para ocultar el panel de eventos mientras se crea
    var creandoEvento by mutableStateOf(false)
        private set

    val eventos = mutableStateListOf<Evento>()
    private var esperandoAsunto = false
    var onHablar: ((String) -> Unit)? = null

    init {
        cargarEventos()
    }

    // ─── Carga de eventos ────────────────────────────────────────────────────
    private fun cargarEventos() {
        viewModelScope.launch {
            val listaEventos = repository.obtenerTodos(usuarioId)
            eventos.clear()
            eventos.addAll(listaEventos)
        }
    }

    // ─── Selección de día en el calendario ──────────────────────────────────
    fun seleccionarDia(fecha: LocalDate) {
        // Solo permite seleccionar día manualmente si no estamos creando un evento
        if (!creandoEvento) {
            diaSeleccionado = fecha
        }
    }

    // ─── Entrada de texto por voz ────────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    fun onTextoRecibido(texto: String) {
        val textoLimpio = texto.lowercase().trim()

        Log.d("Recordatorio", "Texto recibido: $textoLimpio")
        Log.d("Recordatorio", "esperandoAsunto: $esperandoAsunto")
        Log.d("Recordatorio", "diaSeleccionado: $diaSeleccionado")

        if (esperandoAsunto) {
            guardarEvento(textoLimpio)
            return
        }

        procesarComando(textoLimpio)
    }

    // ─── Procesamiento de comandos ───────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    private fun procesarComando(texto: String) {
        val idioma = TextoAVoz.localeActual.language

        val comandosCrear = comandosCrearPorIdioma(idioma)
        val prefijosEliminar = prefijosEliminarPorIdioma(idioma)

        when {
            comandosCrear.any { texto.contains(it) } -> {
                val fecha = extraerFecha(texto)
                if (fecha != null) {
                    diaSeleccionado = fecha
                    esperandoAsunto = true
                    creandoEvento = true           // ← oculta panel en la UI
                    onHablar?.invoke(mensajePedirAsunto(idioma))
                } else {
                    onHablar?.invoke(mensajeFechaNoReconocida(idioma))
                }
            }

            prefijosEliminar.any { texto.startsWith(it) } -> {
                val prefijoUsado = prefijosEliminar.first { texto.startsWith(it) }
                val asunto = texto.removePrefix(prefijoUsado).trim()
                // Busca el evento ignorando mayúsculas/minúsculas y espacios extra
                val evento = eventos.find { it.asunto.trim().equals(asunto, ignoreCase = true) }
                if (evento != null) {
                    eliminarEvento(evento)
                } else {
                    onHablar?.invoke("${mensajeNoEncontrado(idioma)} $asunto")
                }
            }
        }
    }

    // ─── Guardar evento ──────────────────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    private fun guardarEvento(asunto: String) {
        val dia = diaSeleccionado ?: run {
            // Seguridad: si por alguna razón diaSeleccionado es null, reseteamos
            esperandoAsunto = false
            creandoEvento = false
            return
        }
        val idioma = TextoAVoz.localeActual.language

        val evento = Evento(
            asunto = asunto,
            dia = dia.dayOfMonth,
            mes = dia.monthValue,
            anio = dia.year
        )

        viewModelScope.launch {
            try {
                repository.agregar(usuarioId, evento)
                eventos.add(evento)
                onHablar?.invoke(mensajeEventoGuardado(idioma, asunto, dia))
            } catch (e: Exception) {
                Log.e("Recordatorio", "Error guardando evento", e)
                onHablar?.invoke(mensajeErrorGuardar(idioma))
            } finally {
                // Siempre reseteamos el estado al terminar, con éxito o error
                esperandoAsunto = false
                creandoEvento = false
                diaSeleccionado = null
            }
        }
    }

    // ─── Eliminar evento ─────────────────────────────────────────────────────
    fun eliminarEvento(evento: Evento) {
        val idioma = TextoAVoz.localeActual.language
        viewModelScope.launch {
            try {
                repository.eliminar(usuarioId, evento.id)
                eventos.remove(evento)
                onHablar?.invoke(mensajeEventoEliminado(idioma, evento.asunto))
            } catch (e: Exception) {
                Log.e("Recordatorio", "Error eliminando evento", e)
                onHablar?.invoke(mensajeErrorEliminar(idioma))
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EXTRACCIÓN DE FECHA
    // ═════════════════════════════════════════════════════════════════════════

    @RequiresApi(Build.VERSION_CODES.O)
    private fun extraerFecha(texto: String): LocalDate? {
        val idioma = TextoAVoz.localeActual.language
        val meses = mesesPorIdioma(idioma)

        // Primero intentamos tokens compuestos con guion para inglés
        // antes de hacer split por espacios (ej: "twenty-third")
        val textoNormalizado = if (idioma == "en") {
            normalizarCompuestosEN(texto)
        } else texto

        val partes = textoNormalizado.split(" ")

        var dia: Int? = null
        var mes: Int? = null
        var anio: Int? = null

        partes.forEach { parte ->
            val numero = parte.toIntOrNull()

            // Año (4 dígitos entre 2024 y 2099)
            if (numero != null && numero in 2024..2099) {
                anio = numero
                return@forEach
            }

            // Día como número directo
            if (numero != null && numero in 1..31 && dia == null) {
                dia = numero
                return@forEach
            }

            // Día como palabra
            val numPalabra = palabraADia(parte, idioma)
            if (numPalabra != null && dia == null) {
                dia = numPalabra
                return@forEach
            }

            // Mes por nombre
            meses[parte]?.let { if (mes == null) mes = it }
        }

        // Año en palabras (inglés: "two thousand twenty-seven")
        if (anio == null && idioma == "en") {
            anio = extraerAnioPalabrasEN(texto)
        }

        Log.d("Recordatorio", "extraerFecha → dia=$dia mes=$mes anio=$anio")

        return if (dia != null && mes != null) {
            try {
                LocalDate.of(anio ?: LocalDate.now().year, mes!!, dia!!)
            } catch (e: Exception) {
                Log.e("Recordatorio", "Fecha inválida: dia=$dia mes=$mes", e)
                null
            }
        } else null
    }

    /**
     * Reemplaza ordinales compuestos con espacio por versiones con guion
     * para que el split posterior los trate como un único token.
     * Ej: "twenty third" → "twenty-third"
     */
    private fun normalizarCompuestosEN(texto: String): String {
        val unidades = listOf(
            "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
            "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth",
            "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth",
            "nineteenth"
        )
        var resultado = texto
        for (u in unidades) {
            resultado = resultado.replace("twenty $u", "twenty-$u")
            resultado = resultado.replace("thirty $u", "thirty-$u")
        }
        return resultado
    }

    private fun extraerAnioPalabrasEN(texto: String): Int? {
        val miles = mapOf("two thousand" to 2000, "three thousand" to 3000)
        val decenas = mapOf(
            "twenty" to 20, "thirty" to 30, "forty" to 40,
            "fifty" to 50, "sixty" to 60, "seventy" to 70,
            "eighty" to 80, "ninety" to 90
        )
        val unidades = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
            "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
            "eighteen" to 18, "nineteen" to 19
        )

        var base: Int? = null
        for ((k, v) in miles) {
            if (texto.contains(k)) { base = v; break }
        }
        base ?: return null

        val idxThousand = texto.indexOf("thousand")
        var resto = 0
        for ((k, v) in decenas) {
            val idx = texto.indexOf(k)
            if (idx != -1 && idx > idxThousand) { resto += v; break }
        }
        for ((k, v) in unidades) {
            val idx = texto.indexOf(k)
            if (idx != -1 && idx > idxThousand) { resto += v; break }
        }

        return base + resto
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MAPAS DE IDIOMA
    // ═════════════════════════════════════════════════════════════════════════

    private fun mesesPorIdioma(idioma: String): Map<String, Int> = when (idioma) {
        "en" -> mapOf(
            "january" to 1, "february" to 2, "march" to 3, "april" to 4,
            "may" to 5, "june" to 6, "july" to 7, "august" to 8,
            "september" to 9, "october" to 10, "november" to 11, "december" to 12
        )
        "fr" -> mapOf(
            "janvier" to 1, "février" to 2, "mars" to 3, "avril" to 4,
            "mai" to 5, "juin" to 6, "juillet" to 7, "août" to 8,
            "septembre" to 9, "octobre" to 10, "novembre" to 11, "décembre" to 12
        )
        "de" -> mapOf(
            "januar" to 1, "februar" to 2, "märz" to 3, "april" to 4,
            "mai" to 5, "juni" to 6, "juli" to 7, "august" to 8,
            "september" to 9, "oktober" to 10, "november" to 11, "dezember" to 12
        )
        "it" -> mapOf(
            "gennaio" to 1, "febbraio" to 2, "marzo" to 3, "aprile" to 4,
            "maggio" to 5, "giugno" to 6, "luglio" to 7, "agosto" to 8,
            "settembre" to 9, "ottobre" to 10, "novembre" to 11, "dicembre" to 12
        )
        "pt" -> mapOf(
            "janeiro" to 1, "fevereiro" to 2, "março" to 3, "abril" to 4,
            "maio" to 5, "junho" to 6, "julho" to 7, "agosto" to 8,
            "setembro" to 9, "outubro" to 10, "novembro" to 11, "dezembro" to 12
        )
        else -> mapOf(
            "enero" to 1, "febrero" to 2, "marzo" to 3, "abril" to 4,
            "mayo" to 5, "junio" to 6, "julio" to 7, "agosto" to 8,
            "septiembre" to 9, "octubre" to 10, "noviembre" to 11, "diciembre" to 12
        )
    }

    private fun palabraADia(palabra: String, idioma: String): Int? {
        val enOrdinal = mapOf(
            "first" to 1, "second" to 2, "third" to 3, "fourth" to 4,
            "fifth" to 5, "sixth" to 6, "seventh" to 7, "eighth" to 8,
            "ninth" to 9, "tenth" to 10, "eleventh" to 11, "twelfth" to 12,
            "thirteenth" to 13, "fourteenth" to 14, "fifteenth" to 15,
            "sixteenth" to 16, "seventeenth" to 17, "eighteenth" to 18,
            "nineteenth" to 19, "twentieth" to 20,
            "twenty-first" to 21, "twenty-second" to 22, "twenty-third" to 23,
            "twenty-fourth" to 24, "twenty-fifth" to 25, "twenty-sixth" to 26,
            "twenty-seventh" to 27, "twenty-eighth" to 28, "twenty-ninth" to 29,
            "thirtieth" to 30, "thirty-first" to 31
        )
        val enCardinal = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
            "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18,
            "nineteen" to 19, "twenty" to 20, "thirty" to 30
        )
        val esOrdinal = mapOf(
            "primero" to 1, "segundo" to 2, "tercero" to 3, "cuarto" to 4,
            "quinto" to 5, "sexto" to 6, "séptimo" to 7, "octavo" to 8,
            "noveno" to 9, "décimo" to 10, "undécimo" to 11, "duodécimo" to 12,
            "decimotercero" to 13, "decimocuarto" to 14, "decimoquinto" to 15,
            "decimosexto" to 16, "decimoséptimo" to 17, "decimoctavo" to 18,
            "decimonoveno" to 19, "vigésimo" to 20, "veintiuno" to 21,
            "veintidós" to 22, "veintitrés" to 23, "veinticuatro" to 24,
            "veinticinco" to 25, "veintiséis" to 26, "veintisiete" to 27,
            "veintiocho" to 28, "veintinueve" to 29, "treinta" to 30
        )
        val esCardinal = mapOf(
            "uno" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
            "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10,
            "once" to 11, "doce" to 12, "trece" to 13, "catorce" to 14,
            "quince" to 15, "dieciséis" to 16, "diecisiete" to 17,
            "dieciocho" to 18, "diecinueve" to 19, "veinte" to 20
        )
        val frOrdinal = mapOf(
            "premier" to 1, "première" to 1, "deuxième" to 2, "troisième" to 3,
            "quatrième" to 4, "cinquième" to 5, "sixième" to 6, "septième" to 7,
            "huitième" to 8, "neuvième" to 9, "dixième" to 10, "onzième" to 11,
            "douzième" to 12, "treizième" to 13, "quatorzième" to 14,
            "quinzième" to 15, "seizième" to 16, "dix-septième" to 17,
            "dix-huitième" to 18, "dix-neuvième" to 19, "vingtième" to 20,
            "vingt-et-unième" to 21, "vingt-deuxième" to 22, "vingt-troisième" to 23,
            "vingt-quatrième" to 24, "vingt-cinquième" to 25, "vingt-sixième" to 26,
            "vingt-septième" to 27, "vingt-huitième" to 28, "vingt-neuvième" to 29,
            "trentième" to 30, "trente-et-unième" to 31
        )
        val frCardinal = mapOf(
            "un" to 1, "deux" to 2, "trois" to 3, "quatre" to 4, "cinq" to 5,
            "six" to 6, "sept" to 7, "huit" to 8, "neuf" to 9, "dix" to 10,
            "onze" to 11, "douze" to 12, "treize" to 13, "quatorze" to 14,
            "quinze" to 15, "seize" to 16, "dix-sept" to 17, "dix-huit" to 18,
            "dix-neuf" to 19, "vingt" to 20, "vingt-et-un" to 21,
            "vingt-deux" to 22, "vingt-trois" to 23, "vingt-quatre" to 24,
            "vingt-cinq" to 25, "vingt-six" to 26, "vingt-sept" to 27,
            "vingt-huit" to 28, "vingt-neuf" to 29, "trente" to 30, "trente-et-un" to 31
        )
        val deOrdinal = mapOf(
            "erste" to 1, "zweite" to 2, "dritte" to 3, "vierte" to 4,
            "fünfte" to 5, "sechste" to 6, "siebte" to 7, "achte" to 8,
            "neunte" to 9, "zehnte" to 10, "elfte" to 11, "zwölfte" to 12,
            "dreizehnte" to 13, "vierzehnte" to 14, "fünfzehnte" to 15,
            "sechzehnte" to 16, "siebzehnte" to 17, "achtzehnte" to 18,
            "neunzehnte" to 19, "zwanzigste" to 20, "einundzwanzigste" to 21,
            "zweiundzwanzigste" to 22, "dreiundzwanzigste" to 23,
            "vierundzwanzigste" to 24, "fünfundzwanzigste" to 25,
            "sechsundzwanzigste" to 26, "siebenundzwanzigste" to 27,
            "achtundzwanzigste" to 28, "neunundzwanzigste" to 29,
            "dreißigste" to 30, "einunddreißigste" to 31
        )
        val deCardinal = mapOf(
            "eins" to 1, "zwei" to 2, "drei" to 3, "vier" to 4, "fünf" to 5,
            "sechs" to 6, "sieben" to 7, "acht" to 8, "neun" to 9, "zehn" to 10,
            "elf" to 11, "zwölf" to 12, "dreizehn" to 13, "vierzehn" to 14,
            "fünfzehn" to 15, "sechzehn" to 16, "siebzehn" to 17, "achtzehn" to 18,
            "neunzehn" to 19, "zwanzig" to 20, "einundzwanzig" to 21,
            "zweiundzwanzig" to 22, "dreiundzwanzig" to 23, "vierundzwanzig" to 24,
            "fünfundzwanzig" to 25, "sechsundzwanzig" to 26,
            "siebenundzwanzig" to 27, "achtundzwanzig" to 28,
            "neunundzwanzig" to 29, "dreißig" to 30, "einunddreißig" to 31
        )
        val itOrdinal = mapOf(
            "primo" to 1, "secondo" to 2, "terzo" to 3, "quarto" to 4,
            "quinto" to 5, "sesto" to 6, "settimo" to 7, "ottavo" to 8,
            "nono" to 9, "decimo" to 10, "undicesimo" to 11, "dodicesimo" to 12,
            "tredicesimo" to 13, "quattordicesimo" to 14, "quindicesimo" to 15,
            "sedicesimo" to 16, "diciassettesimo" to 17, "diciottesimo" to 18,
            "diciannovesimo" to 19, "ventesimo" to 20, "ventunesimo" to 21,
            "ventiduesimo" to 22, "ventitreesimo" to 23, "ventiquattresimo" to 24,
            "venticinquesimo" to 25, "ventiseiesimo" to 26, "ventisettesimo" to 27,
            "ventottesimo" to 28, "ventinovesimo" to 29, "trentesimo" to 30,
            "trentunesimo" to 31
        )
        val itCardinal = mapOf(
            "uno" to 1, "due" to 2, "tre" to 3, "quattro" to 4, "cinque" to 5,
            "sei" to 6, "sette" to 7, "otto" to 8, "nove" to 9, "dieci" to 10,
            "undici" to 11, "dodici" to 12, "tredici" to 13, "quattordici" to 14,
            "quindici" to 15, "sedici" to 16, "diciassette" to 17, "diciotto" to 18,
            "diciannove" to 19, "venti" to 20, "ventuno" to 21, "ventidue" to 22,
            "ventitre" to 23, "ventiquattro" to 24, "venticinque" to 25,
            "ventisei" to 26, "ventisette" to 27, "ventotto" to 28,
            "ventinove" to 29, "trenta" to 30, "trentuno" to 31
        )
        val ptOrdinal = mapOf(
            "primeiro" to 1, "segundo" to 2, "terceiro" to 3, "quarto" to 4,
            "quinto" to 5, "sexto" to 6, "sétimo" to 7, "oitavo" to 8,
            "nono" to 9, "décimo" to 10, "vigésimo" to 20,
            "vigésimo primeiro" to 21, "vigésimo segundo" to 22,
            "vigésimo terceiro" to 23, "vigésimo quarto" to 24,
            "vigésimo quinto" to 25, "vigésimo sexto" to 26,
            "vigésimo sétimo" to 27, "vigésimo oitavo" to 28,
            "vigésimo nono" to 29, "trigésimo" to 30, "trigésimo primeiro" to 31
        )
        val ptCardinal = mapOf(
            "um" to 1, "dois" to 2, "três" to 3, "quatro" to 4, "cinco" to 5,
            "seis" to 6, "sete" to 7, "oito" to 8, "nove" to 9, "dez" to 10,
            "onze" to 11, "doze" to 12, "treze" to 13, "catorze" to 14,
            "quinze" to 15, "dezesseis" to 16, "dezessete" to 17,
            "dezoito" to 18, "dezenove" to 19, "vinte" to 20,
            "trinta" to 30
        )

        return when (idioma) {
            "en" -> enOrdinal[palabra] ?: enCardinal[palabra]
            "es" -> esOrdinal[palabra] ?: esCardinal[palabra]
            "fr" -> frOrdinal[palabra] ?: frCardinal[palabra]
            "de" -> deOrdinal[palabra] ?: deCardinal[palabra]
            "it" -> itOrdinal[palabra] ?: itCardinal[palabra]
            "pt" -> ptOrdinal[palabra] ?: ptCardinal[palabra]
            else -> null
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COMANDOS POR IDIOMA
    // ═════════════════════════════════════════════════════════════════════════

    private fun comandosCrearPorIdioma(idioma: String) = when (idioma) {
        "en" -> listOf("create event", "add event", "new event", "schedule event")
        "fr" -> listOf("créer événement", "ajouter événement", "nouvel événement", "planifier événement")
        "de" -> listOf("ereignis erstellen", "ereignis hinzufügen", "neues ereignis", "termin erstellen")
        "it" -> listOf("crea evento", "aggiungi evento", "nuovo evento", "pianifica evento")
        "pt" -> listOf("criar evento", "adicionar evento", "novo evento", "agendar evento")
        else -> listOf("crea evento", "añade evento", "crear evento", "nuevo evento", "agregar evento")
    }

    private fun prefijosEliminarPorIdioma(idioma: String) = when (idioma) {
        "en" -> listOf("delete", "remove", "erase", "cancel")
        "fr" -> listOf("supprime", "enlève", "efface", "annule")
        "de" -> listOf("lösche", "entferne", "streiche", "absage")
        "it" -> listOf("elimina", "rimuovi", "cancella", "annulla")
        "pt" -> listOf("elimina", "remove", "apaga", "cancela")
        else -> listOf("elimina", "quita", "borra", "cancela")
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MENSAJES TTS POR IDIOMA
    // ═════════════════════════════════════════════════════════════════════════

    private fun mensajePedirAsunto(idioma: String) = when (idioma) {
        "en" -> "What do you want to add as the subject?"
        "fr" -> "Que voulez-vous ajouter comme sujet?"
        "de" -> "Was möchten Sie als Betreff hinzufügen?"
        "it" -> "Cosa vuoi aggiungere come oggetto?"
        "pt" -> "O que você quer adicionar como assunto?"
        else -> "¿Qué quieres añadir como asunto?"
    }

    private fun mensajeFechaNoReconocida(idioma: String) = when (idioma) {
        "en" -> "I couldn't recognize the date. Please say the day and month clearly."
        "fr" -> "Je n'ai pas reconnu la date. Veuillez dire le jour et le mois clairement."
        "de" -> "Ich konnte das Datum nicht erkennen. Bitte sagen Sie Tag und Monat deutlich."
        "it" -> "Non ho riconosciuto la data. Per favore dì il giorno e il mese chiaramente."
        "pt" -> "Não reconheci a data. Por favor diga o dia e o mês claramente."
        else -> "No reconocí la fecha. Por favor di el día y el mes claramente."
    }

    private fun mensajeNoEncontrado(idioma: String) = when (idioma) {
        "en" -> "I couldn't find any event called"
        "fr" -> "Je n'ai trouvé aucun événement appelé"
        "de" -> "Ich konnte kein Ereignis namens finden"
        "it" -> "Non ho trovato nessun evento chiamato"
        "pt" -> "Não encontrei nenhum evento chamado"
        else -> "No encontré ningún evento llamado"
    }

    private fun mensajeErrorGuardar(idioma: String) = when (idioma) {
        "en" -> "There was an error saving the event. Please try again."
        "fr" -> "Une erreur s'est produite lors de l'enregistrement. Veuillez réessayer."
        "de" -> "Beim Speichern ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut."
        "it" -> "Si è verificato un errore durante il salvataggio. Riprova."
        "pt" -> "Ocorreu um erro ao salvar o evento. Por favor tente novamente."
        else -> "Hubo un error al guardar el evento. Por favor inténtalo de nuevo."
    }

    private fun mensajeErrorEliminar(idioma: String) = when (idioma) {
        "en" -> "There was an error deleting the event. Please try again."
        "fr" -> "Une erreur s'est produite lors de la suppression. Veuillez réessayer."
        "de" -> "Beim Löschen ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut."
        "it" -> "Si è verificato un errore durante l'eliminazione. Riprova."
        "pt" -> "Ocorreu um erro ao eliminar o evento. Por favor tente novamente."
        else -> "Hubo un error al eliminar el evento. Por favor inténtalo de nuevo."
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mensajeEventoGuardado(idioma: String, asunto: String, dia: LocalDate) = when (idioma) {
        "en" -> "Event $asunto saved for ${dia.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dia.dayOfMonth}"
        "fr" -> "Événement $asunto enregistré pour le ${dia.dayOfMonth} ${nombreMesFr(dia.monthValue)}"
        "de" -> "Ereignis $asunto gespeichert für den ${dia.dayOfMonth}. ${nombreMesDe(dia.monthValue)}"
        "it" -> "Evento $asunto salvato per il ${dia.dayOfMonth} ${nombreMesIt(dia.monthValue)}"
        "pt" -> "Evento $asunto salvo para ${dia.dayOfMonth} de ${nombreMesPt(dia.monthValue)}"
        else -> "Evento $asunto guardado para el ${dia.dayOfMonth} de ${nombreMesEs(dia.monthValue)}"
    }

    private fun mensajeEventoEliminado(idioma: String, asunto: String) = when (idioma) {
        "en" -> "Event $asunto deleted"
        "fr" -> "Événement $asunto supprimé"
        "de" -> "Ereignis $asunto gelöscht"
        "it" -> "Evento $asunto eliminato"
        "pt" -> "Evento $asunto eliminado"
        else -> "Evento $asunto eliminado"
    }

    // ═════════════════════════════════════════════════════════════════════════
    // NOMBRES DE MESES PARA TTS
    // ═════════════════════════════════════════════════════════════════════════

    private fun nombreMesEs(mes: Int) = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )[mes - 1]

    private fun nombreMesFr(mes: Int) = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )[mes - 1]

    private fun nombreMesDe(mes: Int) = listOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    )[mes - 1]

    private fun nombreMesIt(mes: Int) = listOf(
        "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
        "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre"
    )[mes - 1]

    private fun nombreMesPt(mes: Int) = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    )[mes - 1]
}