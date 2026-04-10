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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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

    //Conecta al ViewModel con TextoAVoz
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Calendario(viewModel = viewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendario(viewModel: RecordatorioViewModel) {
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    val hoy = LocalDate.now()

    Column {
        // Cabecera del calendario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono para retroceder de mes
            IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
            }
            //Mes actual
            Text(
                text = mesActual.month
                    .getDisplayName(TextStyle.FULL, Locale("es"))
                    .replaceFirstChar { it.uppercase() } + " ${mesActual.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            // Icono para avanzar de mes
            IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Días de la semana del calendario
        val diasSemana = stringArrayResource(R.array.dias_semana)
        Row(modifier = Modifier.fillMaxWidth()) {
            diasSemana.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

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

                        // Eventos de este día
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
                                        tieneEventos -> Color(0xFF4CAF50).copy(alpha = 0.25f)
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
                                        tieneEventos && !esHoy -> Color(0xFF4CAF50)
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable { viewModel.seleccionarDia(fecha) }
                        ) {
                            Text(
                                text = dia.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (esHoy || tieneEventos) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    esHoy -> MaterialTheme.colorScheme.onPrimary
                                    tieneEventos -> Color(0xFF2E7D32)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Eventos del día seleccionado
        val eventosDia = viewModel.diaSeleccionado?.let { fecha ->
            viewModel.eventos.filter {
                it.dia == fecha.dayOfMonth &&
                        it.mes == fecha.monthValue &&
                        it.anio == fecha.year
            }
        } ?: emptyList()

        //Titulo
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
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            //Si no hay evento para el dia seleccionado mostrar el siguiente mensaje
            if (eventosDia.isEmpty()) {
                Text(
                    text = stringResource(R.string.txt_sin_eventos_dia_seleccionado),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
                //Si no mostrar una lista con los eventos asignados a ese dia seleccionado
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(eventosDia) { evento ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            //Asunto del evento
                            Text(
                                text = evento.asunto.replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            //Icono para eliminar evento
                            IconButton(onClick = { viewModel.eliminarEvento(evento) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.icono_eliminar_evento),
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}