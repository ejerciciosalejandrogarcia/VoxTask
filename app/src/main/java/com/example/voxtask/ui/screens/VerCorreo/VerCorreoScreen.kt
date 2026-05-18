package com.example.voxtask.ui.screens.VerCorreo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.anchoMaximoContenido
import androidx.compose.foundation.layout.widthIn
/**
 * Elimina etiquetas HTML del texto para mostrar solo el contenido legible.
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

@Composable
fun VerCorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: VerCorreoViewModel,
    navController: NavController,
    correoId: String?
) {
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos desde dimens.xml
    val paddingContenido        = dimensionResource(R.dimen.ver_correo_padding)
    val paddingHorizontal       = dimensionResource(R.dimen.ver_correo_padding_horizontal)
    val paddingVerticalAsunto   = dimensionResource(R.dimen.ver_correo_padding_vertical_asunto)
    val paddingVerticalRemit    = dimensionResource(R.dimen.ver_correo_padding_vertical_remitente)
    val tamanoAvatar            = dimensionResource(R.dimen.ver_correo_avatar)
    val espaciadoRemitente      = dimensionResource(R.dimen.ver_correo_espaciado_remitente)
    val anchoMaximo             = tamano.anchoMaximoContenido

    LaunchedEffect(correoId) {
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
            when {
                viewModel.cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viewModel.error != null -> {
                    Text(
                        text = viewModel.error!!,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                viewModel.correo == null -> {
                    Text(
                        text = stringResource(R.string.txt_correo_no_encontrado),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    val correo = viewModel.correo!!
                    val cuerpoLimpio = quitarHtml(correo.cuerpo)

                    // Limita el ancho en tabletas y plegables
                    val modificadorContenido = if (anchoMaximo != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier
                            .widthIn(max = anchoMaximo)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    } else {
                        Modifier.fillMaxWidth()
                    }

                    Card(
                        modifier = modificadorContenido
                            .verticalScroll(rememberScrollState()),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {

                            // ── Asunto ────────────────────────────────────────────────────
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = paddingHorizontal,
                                    vertical = paddingVerticalAsunto
                                )
                            ) {
                                Text(
                                    text = correo.asunto,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(thickness = 0.5.dp)

                            // ── Remitente ─────────────────────────────────────────────────
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = paddingHorizontal,
                                        vertical = paddingVerticalRemit
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(espaciadoRemitente)
                            ) {
                                val inicial = correo.remitente
                                    .trim()
                                    .firstOrNull()
                                    ?.uppercaseChar()
                                    ?.toString()
                                    ?: "?"

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
                                        text = inicial,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = correo.remitente,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!correo.emailRemitente.isNullOrBlank()) {
                                        Text(
                                            text = correo.emailRemitente,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp)

                            // ── Cuerpo (siempre texto plano) ──────────────────────────────
                            Text(
                                text = cuerpoLimpio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(paddingHorizontal)
                            )
                        }
                    }
                }
            }
        }
    }
}