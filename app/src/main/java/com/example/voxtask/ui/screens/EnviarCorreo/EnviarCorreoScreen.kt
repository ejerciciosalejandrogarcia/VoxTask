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
import androidx.compose.ui.zIndex
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

@Composable
private fun esHorizontal(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp > config.screenHeightDp
}

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
    val contexto          = LocalContext.current
    val espaciado         = LocalEspaciado.current
    val tamano            = LocalTamanioPantalla.current
    val horizontal        = esHorizontal()
    val snackbarHostState = remember { SnackbarHostState() }

    val paddingHorizontal = dimensionResource(R.dimen.enviar_correo_padding_horizontal)
    val paddingVertical   = paddingVerticalAdaptativo(horizontal)
    val anchoMax          = tamano.anchoMaximoContenido

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

    // Escucha el canal de errores y muestra el Snackbar
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect { mensajeError ->
            snackbarHostState.showSnackbar(
                message  = mensajeError,
                duration = SnackbarDuration.Short
            )
        }
    }

    PlantillaBase(
        viewModel     = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.procesarVoz(texto, contexto) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = paddingHorizontal, vertical = paddingVertical)
        ) {

            // Snackbar anclado en TopCenter
            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = espaciado.xl)
                    .zIndex(10f)
            )

            // Indicador de pasos
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
                    text     = stringResource(R.string.txt_enviarcorreo_paso_uno, viewModel.paso.ordinal + 1),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = espaciado.s),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = Color.Gray
                )
            }

            val modContenido = if (anchoMax != Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMax)
                    .fillMaxSize()
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Column(
                modifier            = modContenido.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(if (horizontal) espaciado.s else espaciado.xl))

                when {
                    viewModel.cargandoToken -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(espaciado.l))
                        Text(
                            stringResource(R.string.txt_enviarcorreo_vincular_cuenta),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

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
                                // Cuenta atrás automática
                                var segundos by remember { mutableStateOf(5) }

                                LaunchedEffect(Unit) {
                                    while (segundos > 0) {
                                        kotlinx.coroutines.delay(1000)
                                        segundos--
                                    }
                                    viewModel.reiniciar(contexto)
                                }

                                Text(
                                    text       = stringResource(R.string.txt_enviarcorreo_exito),
                                    style      = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(espaciado.l))

                                // Círculo con número
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress   = segundos / 5f,
                                        modifier   = Modifier.size(64.dp),
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text  = "$segundos",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(espaciado.l))

                                // Botón para saltarse la espera
                                Button(
                                    onClick  = { viewModel.reiniciar(contexto) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.txt_enviarcorreo_btn_otro), color = Color.White)
                                }
                            }
                            PasoEnvio.ERROR -> {
                                Button(
                                    onClick  = { viewModel.enviarCorreo(contexto) },
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Text(
                                        stringResource(R.string.txt_enviarcorreo_btn_enviarcorreo),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(espaciado.l))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// VincularGoogleUI
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

    if (horizontal && tamano == TamanioPantalla.COMPACTO) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(paddingVincular),
            horizontalArrangement = Arrangement.spacedBy(espaciado.l),
            verticalAlignment     = Alignment.CenterVertically
        ) {
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
            Button(onClick = onVincular, modifier = Modifier.widthIn(min = 140.dp)) {
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
// ConfirmacionUI
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
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(espaciado.l),
                verticalAlignment     = Alignment.Top
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    CuerpoConfirmacion(
                        destinatarioEdit, asuntoEdit, mensajeEdit, campoEditando,
                        onCampoEdit          = { campoEditando = it },
                        onDestinatarioChange = { destinatarioEdit = it; campoEditando = null; onEditar("destinatario:$it") },
                        onAsuntoChange       = { asuntoEdit = it;       campoEditando = null; onEditar("asunto:$it") },
                        onMensajeChange      = { mensajeEdit = it;      campoEditando = null; onEditar("mensaje:$it") }
                    )
                }
                Button(
                    onClick  = onConfirmar,
                    modifier = Modifier.widthIn(min = 120.dp).align(Alignment.CenterVertically)
                ) {
                    Text(stringResource(R.string.txt_enviarcorreo_btn_enviar), color = Color.White)
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                CuerpoConfirmacion(
                    destinatarioEdit, asuntoEdit, mensajeEdit, campoEditando,
                    onCampoEdit          = { campoEditando = it },
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
        modifier            = Modifier.padding(espaciado.l),
        verticalArrangement = Arrangement.spacedBy(espaciado.m)
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
// FilaConfirmacion
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
            text     = etiqueta,
            style    = MaterialTheme.typography.labelSmall,
            color    = Color.Gray,
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
// PasoUI
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
                    Text(valor, modifier = Modifier.padding(espaciado.m), fontSize = tamano.textoBody)
                }
            }
        }
    } else {
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
                    Text(valor, modifier = Modifier.padding(espaciado.m), fontSize = tamano.textoBody)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ResumenCorreo
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
            modifier            = Modifier.padding(espaciado.l),
            verticalArrangement = Arrangement.spacedBy(espaciado.xs)
        ) {
            Text(stringResource(R.string.txt_enviarcorreo_resumen_para, para),      fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_asunto, asunto),  fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_modo, modoTexto), fontSize = tamano.textoBody)
        }
    }
}