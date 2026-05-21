package com.example.voxtask.ui.screens.Recordatorio

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.TextoAVoz
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import com.example.voxtask.R
import com.example.voxtask.utils.PlantillaBaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale

// ──────────────────────────────────────────────────────────────────────────────
// Pantalla principal
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordatorioScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: RecordatorioViewModel,
    navController: NavController
) {
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val configuracion = LocalConfiguration.current

    val esLayout2Columnas = when (tamano) {
        TamanioPantalla.EXPANDIDO -> true
        TamanioPantalla.MEDIO     -> true
        TamanioPantalla.COMPACTO  ->
            configuracion.screenWidthDp > configuracion.screenHeightDp
    }

    val paddingContenido = dimensionResource(R.dimen.recordatorio_padding_contenido)
    val anchoMaximoContenido = tamano.anchoMaximoContenido

    @Suppress("NonObservableStateRead")
    val idiomaActual = TextoAVoz.localeActual.language

    LaunchedEffect(Unit) {
        val mensaje = when (idiomaActual) {
            "en" -> "Say 'create event for the day you want'"
            "fr" -> "Dites 'créer événement pour le jour que vous voulez'"
            "de" -> "Sagen Sie 'Ereignis erstellen für den Tag, den Sie möchten'"
            "it" -> "Di 'crea evento per il giorno che vuoi'"
            "pt" -> "Diga 'criar evento para o dia que quiser'"
            else -> "Di 'crea evento para el día que quieras'"
        }
        TextoAVoz.hablar(contexto, mensaje)

        // ✅ Nombre del parámetro distinto ("texto") para evitar shadowing
        viewModel.onHablar = { texto ->
            CoroutineScope(Dispatchers.Main).launch {
                TextoAVoz.hablar(contexto, texto)
            }
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.onTextoRecibido(texto) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(paddingContenido),
            contentAlignment = Alignment.TopCenter
        ) {
            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.widthIn(max = anchoMaximoContenido).fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            }

            if (esLayout2Columnas) {
                Row(
                    modifier = modificadorContenido,
                    horizontalArrangement = Arrangement.spacedBy(espaciado.l)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        CalendarioGrid(viewModel = viewModel)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EventosDia(viewModel = viewModel)
                    }
                }
            } else {
                Column(
                    modifier = modificadorContenido
                        .verticalScroll(rememberScrollState())
                ) {
                    CalendarioGrid(viewModel = viewModel)
                    EventosDia(viewModel = viewModel)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Grid del calendario
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarioGrid(viewModel: RecordatorioViewModel) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    val tamanoTituloMes = tamano.textoTitulo
    val tamanoTextoDia  = tamano.textoBody

    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    val hoy = LocalDate.now()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.icono_mes_anterior))
            }
            Text(
                text = mesActual.month
                    .getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)
                    .replaceFirstChar { it.uppercase() } + " ${mesActual.year}",
                fontSize = tamanoTituloMes,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.icono_mes_siguiente))
            }
        }

        Spacer(modifier = Modifier.height(espaciado.s))

        val diasSemana = stringArrayResource(R.array.dias_semana)
        Row(modifier = Modifier.fillMaxWidth()) {
            diasSemana.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = tamanoTextoDia,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(espaciado.xs))

        val primerDia = mesActual.atDay(1).dayOfWeek.value
        val totalDias = mesActual.lengthOfMonth()
        val celdas    = primerDia - 1 + totalDias
        val filas     = (celdas / 7) + if (celdas % 7 != 0) 1 else 0

        for (fila in 0 until filas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val numeroCelda = fila * 7 + col
                    val dia = numeroCelda - (primerDia - 1) + 1

                    if (dia < 1 || dia > totalDias) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val fecha        = mesActual.atDay(dia)
                        val esHoy        = fecha == hoy
                        val seleccionado = viewModel.diaSeleccionado == fecha
                        val tieneEventos = viewModel.eventos.any {
                            it.dia == dia &&
                                    it.mes == mesActual.monthValue &&
                                    it.anio == mesActual.year
                        }

                        val tamanoIndicadorEvento = dimensionResource(R.dimen.recordatorio_indicador_evento)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        esHoy        -> MaterialTheme.colorScheme.primary
                                        seleccionado -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        tieneEventos -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else         -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if ((seleccionado || tieneEventos) && !esHoy) 1.dp else 0.dp,
                                    color = if ((seleccionado || tieneEventos) && !esHoy)
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.seleccionarDia(fecha) }
                        ) {
                            Text(
                                text = dia.toString(),
                                fontSize = tamanoTextoDia,
                                fontWeight = if (esHoy || tieneEventos) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    esHoy        -> MaterialTheme.colorScheme.onPrimary
                                    tieneEventos -> MaterialTheme.colorScheme.primary
                                    else         -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Panel de eventos del día seleccionado
// ──────────────────────────────────────────────────────────────────────────────

@SuppressLint("NonObservableLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventosDia(viewModel: RecordatorioViewModel) {
    val espaciado         = LocalEspaciado.current
    val tamano            = LocalTamanioPantalla.current

    val tamanoTituloMes       = tamano.textoTitulo
    val tamanoTextoEvento     = tamano.textoBody
    val tamanoIndicadorEvento = dimensionResource(R.dimen.recordatorio_indicador_evento)
    val tamanoIconoEliminar   = dimensionResource(R.dimen.recordatorio_icono_eliminar)
    val paddingFilaEvento     = dimensionResource(R.dimen.recordatorio_padding_fila_evento)

    // ✅ No mostrar nada mientras se está creando un evento por voz
    if (viewModel.creandoEvento) return

    // ✅ No mostrar nada si no hay día seleccionado
    if (viewModel.diaSeleccionado == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = espaciado.l),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.txt_selecciona_dia),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = tamanoTextoEvento,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val eventosDia = viewModel.eventos.filter {
        it.dia == viewModel.diaSeleccionado!!.dayOfMonth &&
                it.mes == viewModel.diaSeleccionado!!.monthValue &&
                it.anio == viewModel.diaSeleccionado!!.year
    }

    Column {
        Spacer(modifier = Modifier.height(espaciado.l))

        Text(
            text = stringResource(
                R.string.txt_eventos_dia_seleccionado,
                viewModel.diaSeleccionado!!.dayOfMonth,
                viewModel.diaSeleccionado!!.month
                    .getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)
                    .replaceFirstChar { it.uppercase() }
            ),
            fontWeight = FontWeight.Bold,
            fontSize = tamanoTituloMes,
            modifier = Modifier.padding(bottom = espaciado.s)
        )

        if (eventosDia.isEmpty()) {
            Text(
                text = stringResource(R.string.txt_sin_eventos_dia_seleccionado),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = tamanoTextoEvento
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(espaciado.xs)
            ) {
                eventosDia.forEach { evento ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = paddingFilaEvento, vertical = espaciado.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tamanoIndicadorEvento)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(espaciado.s))
                        Text(
                            text = evento.asunto.replaceFirstChar { it.uppercase() },
                            fontSize = tamanoTextoEvento,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.eliminarEvento(evento) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.icono_eliminar_evento),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(tamanoIconoEliminar)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (Mantenido por compatibilidad)
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendario(viewModel: RecordatorioViewModel) {
    Column {
        CalendarioGrid(viewModel = viewModel)
        EventosDia(viewModel = viewModel)
    }
}