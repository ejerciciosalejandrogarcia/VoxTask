package com.example.voxtask.ui.screens.VerCorreo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.anchoMaximoContenido
import kotlinx.coroutines.launch
/**
 * Permite eliminar las etiquetas HTML de una cadena de texto, quitar entidades
 * especiales y formatea el resultado para obtener un texto plano
 */
private fun quitarHtml(texto: String): String =
    texto
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .lines()
        .map { it.trim() }
        .dropWhile { it.isEmpty() }
        .joinToString("\n")
        .trim()
/**
 * Pantalla principal
 */
@Composable
fun VerCorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: VerCorreoViewModel,
    navController: NavController,
    correoId: String?
) {
    /** Variables */
    val contexto  = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current
    val uiState   by viewModel.uiState.collectAsState()
    val paddingContenido      = dimensionResource(R.dimen.ver_correo_padding)
    val paddingHorizontal     = dimensionResource(R.dimen.ver_correo_padding_horizontal)
    val paddingVerticalAsunto = dimensionResource(R.dimen.ver_correo_padding_vertical_asunto)
    val paddingVerticalRemit  = dimensionResource(R.dimen.ver_correo_padding_vertical_remitente)
    val tamanoAvatar          = dimensionResource(R.dimen.ver_correo_avatar)
    val espaciadoRemitente    = dimensionResource(R.dimen.ver_correo_espaciado_remitente)
    val anchoMaximo           = tamano.anchoMaximoContenido
    val snackbarHostState = remember { SnackbarHostState() }

    /** Gestiona el SnackBar e inicia la carga de datos del correo cuando el ID está disponible*/
    LaunchedEffect(correoId) {
        launch {
            viewModel.errorFlow.collect { mensaje ->
                snackbarHostState.showSnackbar(
                    message  = mensaje,
                    duration = SnackbarDuration.Short
                )
            }
        }
        correoId?.let { viewModel.obtenerTokenYCorreo(it, contexto) }
    }

    PlantillaBase(viewModel = viewModelPlantilla, navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(paddingContenido)
        ) {

            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = espaciado.xl)
                    .zIndex(10f)
            )

            val modificadorContenido = if (anchoMaximo != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximo)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            when (val estado = uiState) {
                /** Pantalla a la hora de cargar el contenido del correo */
                is VerCorreoUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                /** Mensaje a la hora de que hubiera un error al cargar el correo */
                is VerCorreoUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(paddingContenido)
                    ) {
                        if (estado.esErrorDeCarga) {
                            Button(onClick = { viewModel.reintentar(contexto) }) {
                                Text(stringResource(R.string.txt_btn_reintentar_cargar_correos))
                            }
                        }
                    }
                }

                is VerCorreoUiState.Exito -> {
                    val correo       = estado.correo
                    val cuerpoLimpio = quitarHtml(correo.cuerpo)

                    Card(
                        modifier  = modificadorContenido.verticalScroll(rememberScrollState()),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {

                            /** Asunto */
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = paddingHorizontal,
                                    vertical   = paddingVerticalAsunto
                                )
                            ) {
                                Text(
                                    text       = correo.asunto,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(thickness = 0.5.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = paddingHorizontal,
                                        vertical   = paddingVerticalRemit
                                    ),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(espaciadoRemitente)
                            ) {
                                /** Inicial del remitente */
                                val inicial = correo.remitente
                                    .trim()
                                    .firstOrNull()
                                    ?.uppercaseChar()
                                    ?.toString() ?: "?"

                                Box(
                                    modifier = Modifier
                                        .size(tamanoAvatar)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = inicial,
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign  = TextAlign.Center
                                    )
                                }
                                /** Remitente */
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = correo.remitente,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!correo.emailRemitente.isNullOrBlank()) {
                                        Text(
                                            text  = correo.emailRemitente,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp)

                            /** Cuerpo */
                            Text(
                                text       = cuerpoLimpio,
                                style      = MaterialTheme.typography.bodyMedium,
                                color      = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp,
                                modifier   = Modifier.padding(paddingHorizontal)
                            )
                        }
                    }
                }
            }
        }
    }
}