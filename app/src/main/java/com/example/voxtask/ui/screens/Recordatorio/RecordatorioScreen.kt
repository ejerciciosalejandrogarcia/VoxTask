package com.example.voxtask.ui.screens.Recordatorio

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
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

    // Valores adaptativos
    val paddingContenido = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.l       // 16 dp
        TamanioPantalla.MEDIO     -> espaciado.xl      // 32 dp
        TamanioPantalla.EXPANDIDO -> 48.dp
    }

    // Nuevo: ancho máximo del contenido para tabletas y plegables
    val anchoMaximoContenido = tamano.anchoMaximoContenido

    LaunchedEffect(Unit) {
        TextoAVoz.hablar(contexto, "Di 'crea evento para el día que quieras''")
        viewModel.onHablar = { mensaje ->
            CoroutineScope(Dispatchers.Main).launch {
                TextoAVoz.hablar(contexto, mensaje)
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
                .padding(paddingContenido),             // antes: 16.dp fijo
            contentAlignment = Alignment.TopCenter
        ) {
            // Nuevo: limita el ancho en tabletas y plegables, centrado automático
            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.widthIn(max = anchoMaximoContenido).fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            }

            Column(modifier = modificadorContenido) {
                Calendario(viewModel = viewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendario(viewModel: RecordatorioViewModel) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos
    val tamanoTituloMes = tamano.textoTitulo
    val tamanoTextoDia = tamano.textoBody
    val tamanoTextoEvento = tamano.textoBody
    val tamanoIndicadorEvento = when (tamano) {
        TamanioPantalla.COMPACTO  -> 8.dp
        TamanioPantalla.MEDIO     -> 10.dp
        TamanioPantalla.EXPANDIDO -> 12.dp
    }
    val tamanoIconoEliminar = when (tamano) {
        TamanioPantalla.COMPACTO  -> 18.dp
        TamanioPantalla.MEDIO     -> 22.dp
        TamanioPantalla.EXPANDIDO -> 26.dp
    }
    val paddingFilaEvento = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.m       // 12 dp horizontal, 8 dp vertical
        TamanioPantalla.MEDIO     -> espaciado.l
        TamanioPantalla.EXPANDIDO -> espaciado.xl
    }

    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    val hoy = LocalDate.now()

    Column {
        // Cabecera del calendario
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
                    .getDisplayName(TextStyle.FULL, Locale("es"))
                    .replaceFirstChar { it.uppercase() } + " ${mesActual.year}",
                fontSize = tamanoTituloMes,             // antes: 20.sp fijo
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.icono_mes_siguiente))
            }
        }

        Spacer(modifier = Modifier.height(espaciado.s))    // antes: 8.dp

        // Días de la semana
        val diasSemana = stringArrayResource(R.array.dias_semana)
        Row(modifier = Modifier.fillMaxWidth()) {
            diasSemana.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = tamanoTextoDia,              // antes: 13.sp fijo
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(espaciado.xs))   // antes: 4.dp

        val primerDia = mesActual.atDay(1).dayOfWeek.value
        val totalDias = mesActual.lengthOfMonth()
        val celdas = primerDia - 1 + totalDias
        val filas = (celdas / 7) + if (celdas % 7 != 0) 1 else 0

        for (fila in 0 until filas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val numeroCelda = fila * 7 + col
                    val dia = numeroCelda - (primerDia - 1) + 1

                    if (dia < 1 || dia > totalDias) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val fecha = mesActual.atDay(dia)
                        val esHoy = fecha == hoy
                        val seleccionado = viewModel.diaSeleccionado == fecha
                        val tieneEventos = viewModel.eventos.any {
                            it.dia == dia &&
                                    it.mes == mesActual.monthValue &&
                                    it.anio == mesActual.year
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        esHoy -> MaterialTheme.colorScheme.primary
                                        seleccionado -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        tieneEventos -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = when {
                                        seleccionado && !esHoy -> 1.dp
                                        tieneEventos && !esHoy -> 1.dp
                                        else -> 0.dp
                                    },
                                    color = when {
                                        seleccionado && !esHoy -> MaterialTheme.colorScheme.primary
                                        tieneEventos && !esHoy -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable { viewModel.seleccionarDia(fecha) }
                        ) {
                            Text(
                                text = dia.toString(),
                                fontSize = tamanoTextoDia,                  // antes: 14.sp fijo
                                fontWeight = if (esHoy || tieneEventos) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    esHoy -> MaterialTheme.colorScheme.onPrimary
                                    tieneEventos -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp

        // Eventos del día seleccionado
        val eventosDia = viewModel.diaSeleccionado?.let { fecha ->
            viewModel.eventos.filter {
                it.dia == fecha.dayOfMonth &&
                        it.mes == fecha.monthValue &&
                        it.anio == fecha.year
            }
        } ?: emptyList()

        if (viewModel.diaSeleccionado != null) {
            Text(
                text = stringResource(
                    R.string.txt_eventos_dia_seleccionado,
                    viewModel.diaSeleccionado!!.dayOfMonth,
                    viewModel.diaSeleccionado!!.month
                        .getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercase() }
                ),
                fontWeight = FontWeight.Bold,
                fontSize = tamanoTituloMes,                // antes: 16.sp fijo
                modifier = Modifier.padding(bottom = espaciado.s) // antes: 8.dp
            )

            if (eventosDia.isEmpty()) {
                Text(
                    text = stringResource(R.string.txt_sin_eventos_dia_seleccionado),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = tamanoTextoEvento           // antes: 14.sp fijo
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(espaciado.xs) // antes: 4.dp
                ) {
                    items(eventosDia) { evento ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = paddingFilaEvento, vertical = espaciado.s), // antes: 12.dp, 8.dp
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tamanoIndicadorEvento)               // antes: 8.dp fijo
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(espaciado.s))     // antes: 8.dp
                            Text(
                                text = evento.asunto.replaceFirstChar { it.uppercase() },
                                fontSize = tamanoTextoEvento,                  // antes: 14.sp fijo
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.eliminarEvento(evento) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.icono_eliminar_evento),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(tamanoIconoEliminar) // antes: 18.dp fijo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}