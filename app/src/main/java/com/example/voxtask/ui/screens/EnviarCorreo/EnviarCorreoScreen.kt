package com.example.voxtask.ui.screens.EnviarCorreo

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun EnviarCorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: EnviarCorreoViewModel,
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
            TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_enviarcorreo_paso_destinatario_pregunta))
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.procesarVoz(texto, contexto) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(paddingContenido)
        ) {
            // Indicador de pasos
            if (!viewModel.necesitaVincularGoogle &&
                !viewModel.cargandoToken &&
                viewModel.paso !in listOf(PasoEnvio.ENVIANDO, PasoEnvio.ENVIADO, PasoEnvio.ERROR, PasoEnvio.CONFIRMACION)
            ) {
                Text(
                    text = stringResource(R.string.txt_enviarcorreo_paso_uno, viewModel.paso.ordinal + 1),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = espaciado.s),           // antes: 8.dp
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // Nuevo: limita el ancho en tabletas y plegables, centrado automático
            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximoContenido)
                    .fillMaxSize()
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Column(
                modifier = modificadorContenido,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    viewModel.cargandoToken -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp
                        Text(
                            stringResource(R.string.txt_enviarcorreo_vincular_cuenta),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    viewModel.necesitaVincularGoogle -> {
                        VincularGoogleUI(
                            onVincular = {
                                viewModel.vincularGoogle(contexto) {
                                    lanzadorGoogle.launch(viewModel.obtenerClienteGoogle(contexto).signInIntent)
                                }
                            }
                        )
                    }
                    else -> {
                        when (viewModel.paso) {
                            PasoEnvio.DESTINATARIO -> PasoUI(
                                titulo = stringResource(R.string.txt_enviarcorreo_paso_destinatario_pregunta),
                                descripcion = stringResource(R.string.txt_enviarcorreo_paso_destinatario_description),
                                valor = viewModel.destinatario
                            )
                            PasoEnvio.ASUNTO -> PasoUI(
                                titulo = stringResource(R.string.txt_enviarcorreo_pregunta_asunto),
                                descripcion = stringResource(R.string.txt_enviarcorreo_descripcion_asunto),
                                valor = viewModel.asunto
                            )
                            PasoEnvio.MODO -> PasoUI(
                                titulo = stringResource(R.string.txt_enviarcorreo_pregunta_modo),
                                descripcion = stringResource(R.string.txt_enviarcorreo_descripcion_modo),
                                valor = ""
                            )
                            PasoEnvio.MENSAJE -> PasoUI(
                                titulo = if (viewModel.modo == "ia") stringResource(R.string.txt_enviarcorreo_pregunta_mensaje_ia) else stringResource(R.string.txt_enviarcorreo_pregunta_mensaje_manual),
                                descripcion = if (viewModel.modo == "ia") stringResource(R.string.txt_enviarcorreo_descripcion_mensaje_ia) else stringResource(R.string.txt_enviarcorreo_descripcion_mensaje_manual),
                                valor = viewModel.mensaje
                            )
                            PasoEnvio.CONFIRMACION -> ConfirmacionUI(
                                destinatario = viewModel.destinatario,
                                asunto = viewModel.asunto,
                                mensaje = viewModel.mensaje,
                                modo = viewModel.modo,
                                onConfirmar = { viewModel.confirmarEnvio(contexto) },
                                onEditar = { campo -> viewModel.editarCampo(campo) }
                            )
                            PasoEnvio.ENVIANDO -> {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp
                                Text(
                                    stringResource(R.string.txt_enviarcorreo_estado_enviando),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PasoEnvio.ENVIADO -> {
                                Text(
                                    text = stringResource(R.string.txt_enviarcorreo_exito),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(espaciado.xl))   // antes: 24.dp
                              //  ResumenCorreo(viewModel.destinatario, viewModel.asunto, viewModel.modo)
                                Spacer(modifier = Modifier.height(espaciado.xl))   // antes: 24.dp
                                Button(
                                    onClick = { viewModel.reiniciar(contexto) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.txt_enviarcorreo_btn_otro), color = Color.White)
                                }
                            }
                            PasoEnvio.ERROR -> {
                                Text(
                                    text = viewModel.errorMensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(espaciado.xl))   // antes: 24.dp
                                Button(
                                    onClick = { viewModel.reiniciar(contexto) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.txt_enviarcorreo_btn_reintentar), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VincularGoogleUI(onVincular: () -> Unit) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos
    val paddingVincular = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.xl      // 24 dp
        TamanioPantalla.MEDIO     -> 40.dp
        TamanioPantalla.EXPANDIDO -> 48.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(paddingVincular)    // antes: 32.dp
    ) {
        Text(
            stringResource(R.string.txt_enviarcorreo_google_requerido),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = tamano.textoTitulo
        )
        Spacer(modifier = Modifier.height(espaciado.l))            // antes: 16.dp
        Text(
            stringResource(R.string.txt_enviarcorreo_google_descripcion),
            textAlign = TextAlign.Center,
            fontSize = tamano.textoBody
        )
        Spacer(modifier = Modifier.height(espaciado.xl))           // antes: 24.dp
        Button(onClick = onVincular, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.txt_enviarcorreo_btn_vincular), color = Color.White)
        }
    }
}

@Composable
fun ConfirmacionUI(
    destinatario: String,
    asunto: String,
    mensaje: String,
    modo: String,
    onConfirmar: () -> Unit,
    onEditar: (String) -> Unit
) {
    val espaciado = LocalEspaciado.current

    var campoEditando by remember { mutableStateOf<String?>(null) }
    var destinatarioEdit by remember { mutableStateOf(destinatario) }
    var asuntoEdit by remember { mutableStateOf(asunto) }
    var mensajeEdit by remember { mutableStateOf(mensaje) }

    Column(verticalArrangement = Arrangement.spacedBy(espaciado.l)) {    // antes: 16.dp
        Text(
            stringResource(R.string.txt_enviarcorreo_titulo_confirmacion),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(espaciado.l),              // antes: 16.dp
                verticalArrangement = Arrangement.spacedBy(espaciado.m) // antes: 12.dp
            ) {
                FilaConfirmacion(
                    stringResource(R.string.txt_enviarcorreo_etiqueta_para),
                    destinatarioEdit,
                    campoEditando == "destinatario",
                    { campoEditando = "destinatario" }
                ) { destinatarioEdit = it; campoEditando = null; onEditar("destinatario:$it") }

                FilaConfirmacion(
                    stringResource(R.string.txt_enviarcorreo_etiqueta_asunto),
                    asuntoEdit,
                    campoEditando == "asunto",
                    { campoEditando = "asunto" }
                ) { asuntoEdit = it; campoEditando = null; onEditar("asunto:$it") }

                FilaConfirmacion(
                    stringResource(R.string.txt_enviarcorreo_etiqueta_mensaje),
                    mensajeEdit,
                    campoEditando == "mensaje",
                    { campoEditando = "mensaje" }
                ) { mensajeEdit = it; campoEditando = null; onEditar("mensaje:$it") }
            }
        }
        Button(onClick = onConfirmar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.txt_enviarcorreo_btn_enviar), color = Color.White)
        }
    }
}

@Composable
fun FilaConfirmacion(
    etiqueta: String,
    valor: String,
    editando: Boolean,
    onEditar: () -> Unit,
    onGuardar: (String) -> Unit
) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    var textoTemp by remember(valor) { mutableStateOf(valor) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Etiqueta (Para / Asunto / Mensaje)
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = tamano.textoBody
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (editando) {
            // TextField ocupa todo el ancho
            OutlinedTextField(
                value = textoTemp,
                onValueChange = { textoTemp = it },
                modifier = Modifier.fillMaxWidth()
            )
            // Botón Guardar alineado a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onGuardar(textoTemp) }) {
                    Text(stringResource(R.string.txt_enviarcorreo_btn_guardar))
                }
            }
        } else {
            // Valor y botón Editar en la misma fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyMedium,
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
@Composable
fun PasoUI(titulo: String, descripcion: String, valor: String) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(espaciado.m)        // antes: 12.dp
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontSize = tamano.textoTitulo
        )
        Text(
            descripcion,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = tamano.textoBody
        )
        if (valor.isNotEmpty()) {
            Card {
                Text(
                    valor,
                    modifier = Modifier.padding(espaciado.m),          // antes: 12.dp
                    fontSize = tamano.textoBody
                )
            }
        }
    }
}

@Composable
fun ResumenCorreo(para: String, asunto: String, modo: String) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    val modoTexto = if (modo == "ia")
        stringResource(R.string.txt_enviarcorreo_modo_ia)
    else
        stringResource(R.string.txt_enviarcorreo_modo_manual)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(espaciado.l),                  // antes: 16.dp
            verticalArrangement = Arrangement.spacedBy(espaciado.xs)   // nuevo: separación entre líneas
        ) {
            Text(stringResource(R.string.txt_enviarcorreo_resumen_para, para), fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_asunto, asunto), fontSize = tamano.textoBody)
            Text(stringResource(R.string.txt_enviarcorreo_resumen_modo, modoTexto), fontSize = tamano.textoBody)
        }
    }
}