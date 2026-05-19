package com.example.voxtask.ui.screens.EnviarCorreo

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

// ---------------------------------------------------------------------------
// Helpers de orientación y layout
// ---------------------------------------------------------------------------

/**
 * Devuelve true si el dispositivo está en orientación horizontal (landscape).
 * Se recalcula automáticamente al girar gracias a LocalConfiguration.
 */
@Composable
private fun esHorizontal(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp > config.screenHeightDp
}

/**
 * Padding vertical reducido en landscape para aprovechar la altura limitada.
 */
@Composable
private fun paddingVerticalAdaptativo(horizontal: Boolean): Dp {
    return if (horizontal) {
        dimensionResource(R.dimen.enviar_correo_padding_vertical_landscape)
    } else {
        dimensionResource(R.dimen.enviar_correo_padding_vertical)
    }
}

// ---------------------------------------------------------------------------
// Pantalla principal
// ---------------------------------------------------------------------------

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun EnviarCorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: EnviarCorreoViewModel,
    navController: NavController
) {
    val contexto      = LocalContext.current
    val espaciado     = LocalEspaciado.current
    val tamano        = LocalTamanioPantalla.current
    val horizontal    = esHorizontal()

    // Padding horizontal del contenido
    val paddingHorizontal = dimensionResource(R.dimen.enviar_correo_padding_horizontal)
    val paddingVertical = paddingVerticalAdaptativo(horizontal)

    // Ancho máximo: limita en tabletas y plegables
    val anchoMax = tamano.anchoMaximoContenido

    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
                    .getResult(ApiException::class.java)
                viewModel.guardarToken(contexto)
            } catch (e: ApiException) {
                android.util.Log.e("GOOGLE", "Error: ${e.statusCode}")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar(contexto)
        if (!viewModel.necesitaVincularGoogle) {
            TextoAVoz.hablar(
                contexto,
                contexto.getString(R.string.txt_enviarcorreo_paso_destinatario_pregunta)
            )
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.procesarVoz(texto, contexto) }
    ) { padding ->

        // ── Contenedor externo con scroll siempre habilitado ──────────────────
        // Esto permite deslizar en landscape y en pantallas pequeñas con
        // teclado visible.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = paddingHorizontal, vertical = paddingVertical)
        ) {
            // Indicador de pasos (arriba, centrado)
            if (!viewModel.necesitaVincularGoogle &&
                !viewModel.cargandoToken &&
                viewModel.paso !in listOf(
                    PasoEnvio.ENVIANDO,
                    PasoEnvio.ENVIADO,
                    PasoEnvio.ERROR,
                    PasoEnvio.CONFIRMACION
                )
            ) {
                Text(
                    text = stringResource(R.string.txt_enviarcorreo_paso_uno, viewModel.paso.ordinal + 1),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = espaciado.s),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // ── Modificador central con ancho máximo ─────────────────────────
            val modContenido = if (anchoMax != Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMax)
                    .fillMaxSize()
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            // ── Columna con scroll vertical siempre activo ───────────────────
            // En portrait apenas se nota; en landscape permite deslizar todo.
            Column(
                modifier = modContenido
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Espacio superior mínimo para que el contenido no quede pegado
                // al indicador de pasos en portrait.
                Spacer(modifier = Modifier.height(if (horizontal) espaciado.s else espaciado.xl))

                when {
                    // ── Cargando token ───────────────────────────────────────
                    viewModel.cargandoToken -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(espaciado.l))
                        Text(
                            stringResource(R.string.txt_enviarcorreo_vincular_cuenta),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // ── Vincular Google ──────────────────────────────────────
                    viewModel.necesitaVincularGoogle -> {
                        VincularGoogleUI(
                            horizontal = horizontal,
                            onVincular = {
                                viewModel.vincularGoogle(contexto) {
                                    lanzadorGoogle.launch(
                                        viewModel.obtenerClienteGoogle(contexto).signInIntent
                                    )
                                }
                            }
                        )
                    }

                    // ── Pasos del flujo ──────────────────────────────────────
                    else -> {
                        when (viewModel.paso) {
                            PasoEnvio.DESTINATARIO -> PasoUI(
                                titulo      = stringResource(R.string.txt_enviarcorreo_paso_destinatario_pregunta),
                                descripcion = stringResource(R.string.txt_enviarcorreo_paso_destinatario_description),
                                valor       = viewModel.destinatario,
                                horizontal  = horizontal
                            )
                            PasoEnvio.ASUNTO -> PasoUI(
                                titulo      = stringResource(R.string.txt_enviarcorreo_pregunta_asunto),
                                descripcion = stringResource(R.string.txt_enviarcorreo_descripcion_asunto),
                                valor       = viewModel.asunto,
                                horizontal  = horizontal
                            )
                            PasoEnvio.MODO -> PasoUI(
                                titulo      = stringResource(R.string.txt_enviarcorreo_pregunta_modo),
                                descripcion = stringResource(R.string.txt_enviarcorreo_descripcion_modo),
                                valor       = "",
                                horizontal  = horizontal
                            )
                            PasoEnvio.MENSAJE -> PasoUI(
                                titulo      = if (viewModel.modo == "ia")
                                    stringResource(R.string.txt_enviarcorreo_pregunta_mensaje_ia)
                                else
                                    stringResource(R.string.txt_enviarcorreo_pregunta_mensaje_manual),
                                descripcion = if (viewModel.modo == "ia")
                                    stringResource(R.string.txt_enviarcorreo_descripcion_mensaje_ia)
                                else
                                    stringResource(R.string.txt_enviarcorreo_descripcion_mensaje_manual),
                                valor       = viewModel.mensaje,
                                horizontal  = horizontal
                            )
                            PasoEnvio.CONFIRMACION -> ConfirmacionUI(
                                destinatario = viewModel.destinatario,
                                asunto       = viewModel.asunto,
                                mensaje      = viewModel.mensaje,
                                modo         = viewModel.modo,
                                horizontal   = horizontal,
                                onConfirmar  = { viewModel.confirmarEnvio(contexto) },
                                onEditar     = { campo -> viewModel.editarCampo(campo) }
                            )
                            PasoEnvio.ENVIANDO -> {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(espaciado.l))
                                Text(
                                    stringResource(R.string.txt_enviarcorreo_estado_enviando),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PasoEnvio.ENVIADO -> {
                                Text(
                                    text      = stringResource(R.string.txt_enviarcorreo_exito),
                                    style     = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color     = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(espaciado.xl))
                                Spacer(modifier = Modifier.height(espaciado.xl))
                                Button(
                                    onClick  = { viewModel.reiniciar(contexto) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.txt_enviarcorreo_btn_otro), color = Color.White)
                                }
                            }
                            PasoEnvio.ERROR -> {
                                Text(
                                    text      = viewModel.errorMensaje,
                                    color     = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(espaciado.xl))
                                Button(
                                    onClick  = { viewModel.reiniciar(contexto) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.txt_enviarcorreo_btn_reintentar), color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Espacio inferior para que el último elemento no quede pegado
                // al borde en landscape con barra de navegación.
                Spacer(modifier = Modifier.height(espaciado.l))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// VincularGoogleUI — adaptado a orientación
// ---------------------------------------------------------------------------

@Composable
fun VincularGoogleUI(
    horizontal: Boolean,
    onVincular: () -> Unit
) {
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current

    val paddingVincular = if (horizontal) {
        dimensionResource(R.dimen.enviar_correo_padding_vincular_landscape)
    } else {
        dimensionResource(R.dimen.enviar_correo_padding_vincular)
    }

    // En landscape usa layout de dos columnas (texto | botón)
    if (horizontal && tamano == TamanioPantalla.COMPACTO) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(paddingVincular),
            horizontalArrangement = Arrangement.spacedBy(espaciado.l),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // Columna de texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.txt_enviarcorreo_google_requerido),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(espaciado.s))
                Text(
                    stringResource(R.string.txt_enviarcorreo_google_descripcion),
                    textAlign = TextAlign.Start,
                    fontSize  = tamano.textoBody
                )
            }
            // Botón lateral
            Button(
                onClick  = onVincular,
                modifier = Modifier.widthIn(min = 140.dp)
            ) {
                Text(stringResource(R.string.txt_enviarcorreo_btn_vincular), color = Color.White)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(paddingVincular)
        ) {
            Text(
                stringResource(R.string.txt_enviarcorreo_google_requerido),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                fontSize   = tamano.textoTitulo
            )
            Spacer(modifier = Modifier.height(espaciado.l))
            Text(
                stringResource(R.string.txt_enviarcorreo_google_descripcion),
                textAlign = TextAlign.Center,
                fontSize  = tamano.textoBody
            )
            Spacer(modifier = Modifier.height(espaciado.xl))
            Button(onClick = onVincular, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.txt_enviarcorreo_btn_vincular), color = Color.White)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ConfirmacionUI — adaptado a orientación y tamaño
// ---------------------------------------------------------------------------

@Composable
fun ConfirmacionUI(
    destinatario: String,
    asunto: String,
    mensaje: String,
    modo: String,
    horizontal: Boolean,
    onConfirmar: () -> Unit,
    onEditar: (String) -> Unit
) {
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current

    var campoEditando    by remember { mutableStateOf<String?>(null) }
    var destinatarioEdit by remember { mutableStateOf(destinatario) }
    var asuntoEdit       by remember { mutableStateOf(asunto) }
    var mensajeEdit      by remember { mutableStateOf(mensaje) }

    // En landscape + compacto: Card + Botón en Row para aprovechar el ancho
    val usarLayoutHorizontal = horizontal && tamano == TamanioPantalla.COMPACTO

    Column(verticalArrangement = Arrangement.spacedBy(espaciado.l)) {
        Text(
            stringResource(R.string.txt_enviarcorreo_titulo_confirmacion),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        if (usarLayoutHorizontal) {
            // ── Landscape compacto: Card a la izquierda, botón a la derecha ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(espaciado.l),
                verticalAlignment     = Alignment.Top
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    CuerpoConfirmacion(
                        destinatarioEdit, asuntoEdit, mensajeEdit,
                        campoEditando,
                        onCampoEdit  = { campoEditando = it },
                        onDestinatarioChange = { destinatarioEdit = it; campoEditando = null; onEditar("destinatario:$it") },
                        onAsuntoChange       = { asuntoEdit = it;       campoEditando = null; onEditar("asunto:$it") },
                        onMensajeChange      = { mensajeEdit = it;      campoEditando = null; onEditar("mensaje:$it") }
                    )
                }
                Button(
                    onClick  = onConfirmar,
                    modifier = Modifier
                        .widthIn(min = 120.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(stringResource(R.string.txt_enviarcorreo_btn_enviar), color = Color.White)
                }
            }
        } else {
            // ── Portrait / tableta / plegable: layout vertical normal ─────────
            Card(modifier = Modifier.fillMaxWidth()) {
                CuerpoConfirmacion(
                    destinatarioEdit, asuntoEdit, mensajeEdit,
                    campoEditando,
                    onCampoEdit  = { campoEditando = it },
                    onDestinatarioChange = { destinatarioEdit = it; campoEditando = null; onEditar("destinatario:$it") },
                    onAsuntoChange       = { asuntoEdit = it;       campoEditando = null; onEditar("asunto:$it") },
                    onMensajeChange      = { mensajeEdit = it;      campoEditando = null; onEditar("mensaje:$it") }
                )
            }
            Button(onClick = onConfirmar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.txt_enviarcorreo_btn_enviar), color = Color.White)
            }
        }
    }
}

/** Contenido interior de la Card de confirmación, extraído para reutilizar. */
@Composable
private fun CuerpoConfirmacion(
    destinatarioEdit: String,
    asuntoEdit: String,
    mensajeEdit: String,
    campoEditando: String?,
    onCampoEdit: (String?) -> Unit,
    onDestinatarioChange: (String) -> Unit,
    onAsuntoChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit
) {
    val espaciado = LocalEspaciado.current
    Column(
        modifier              = Modifier.padding(espaciado.l),
        verticalArrangement   = Arrangement.spacedBy(espaciado.m)
    ) {
        FilaConfirmacion(
            etiqueta  = stringResource(R.string.txt_enviarcorreo_etiqueta_para),
            valor     = destinatarioEdit,
            editando  = campoEditando == "destinatario",
            onEditar  = { onCampoEdit("destinatario") },
            onGuardar = onDestinatarioChange
        )
        FilaConfirmacion(
            etiqueta  = stringResource(R.string.txt_enviarcorreo_etiqueta_asunto),
            valor     = asuntoEdit,
            editando  = campoEditando == "asunto",
            onEditar  = { onCampoEdit("asunto") },
            onGuardar = onAsuntoChange
        )
        FilaConfirmacion(
            etiqueta  = stringResource(R.string.txt_enviarcorreo_etiqueta_mensaje),
            valor     = mensajeEdit,
            editando  = campoEditando == "mensaje",
            onEditar  = { onCampoEdit("mensaje") },
            onGuardar = onMensajeChange
        )
    }
}

// ---------------------------------------------------------------------------
// FilaConfirmacion — sin cambios funcionales
// ---------------------------------------------------------------------------

@Composable
fun FilaConfirmacion(
    etiqueta: String,
    valor: String,
    editando: Boolean,
    onEditar: () -> Unit,
    onGuardar: (String) -> Unit
) {
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current

    var textoTemp by remember(valor) { mutableStateOf(valor) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = tamano.textoBody
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (editando) {
            OutlinedTextField(
                value         = textoTemp,
                onValueChange = { textoTemp = it },
                modifier      = Modifier.fillMaxWidth()
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onGuardar(textoTemp) }) {
                    Text(stringResource(R.string.txt_enviarcorreo_btn_guardar))
                }
            }
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text     = valor,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontSize = tamano.textoBody,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEditar) {
                    Text(stringResource(R.string.txt_enviarcorreo_btn_editar))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PasoUI — adaptado a orientación
// ---------------------------------------------------------------------------

@Composable
fun PasoUI(
    titulo: String,
    descripcion: String,
    valor: String,
    horizontal: Boolean
) {
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current

    // En landscape compacto: título + descripción en Row, Card debajo
    if (horizontal && tamano == TamanioPantalla.COMPACTO) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(espaciado.s)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(espaciado.l),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    titulo,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    textAlign  = TextAlign.Start,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    descripcion,
                    textAlign = TextAlign.Start,
                    color     = Color.Gray,
                    fontSize  = tamano.textoBody,
                    modifier  = Modifier.weight(1f)
                )
            }
            if (valor.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        valor,
                        modifier = Modifier.padding(espaciado.m),
                        fontSize = tamano.textoBody
                    )
                }
            }
        }
    } else {
        // Portrait / tableta / plegable: layout vertical original
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(espaciado.m)
        ) {
            Text(
                titulo,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                textAlign  = TextAlign.Center,
                fontSize   = tamano.textoTitulo
            )
            Text(
                descripcion,
                textAlign = TextAlign.Center,
                color     = Color.Gray,
                fontSize  = tamano.textoBody
            )
            if (valor.isNotEmpty()) {
                Card {
                    Text(
                        valor,
                        modifier = Modifier.padding(espaciado.m),
                        fontSize = tamano.textoBody
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ResumenCorreo — sin cambios
// ---------------------------------------------------------------------------

@Composable
fun ResumenCorreo(para: String, asunto: String, modo: String) {
    val espaciado = LocalEspaciado.current
    val tamano    = LocalTamanioPantalla.current

    val modoTexto = if (modo == "ia")
        stringResource(R.string.txt_enviarcorreo_modo_ia)
    else
        stringResource(R.string.txt_enviarcorreo_modo_manual)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier              = Modifier.padding(espaciado.l),
            verticalArrangement   = Arrangement.spacedBy(espaciado.xs)
        ) {
            Text(stringResource(R.string.txt_enviarcorreo_resumen_para, para),   fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_asunto, asunto), fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_modo, modoTexto), fontSize = tamano.textoBody)
        }
    }
}