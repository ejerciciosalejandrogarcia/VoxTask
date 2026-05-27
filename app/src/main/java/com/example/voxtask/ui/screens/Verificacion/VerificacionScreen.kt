package com.example.voxtask.ui.screens.Verificacion

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.theme.*
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
import kotlinx.coroutines.delay

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun VerificacionScreen(
    viewModel: VerificacionViewModel = viewModel(),
    navController: NavController
) {
    val estadoUi by viewModel.estadoUi.collectAsState()
    var codigo by remember { mutableStateOf(List(5) { "" }) }
    val codigoUnido = codigo.joinToString("")
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val configuracion = androidx.compose.ui.platform.LocalConfiguration.current

    // Detectar orientación
    val esLandscape = configuracion.screenWidthDp > configuracion.screenHeightDp

    // Valores adaptativos desde dimens.xml
    val paddingHorizontal  = dimensionResource(R.dimen.verificacion_padding_horizontal)
    val paddingCardH       = dimensionResource(R.dimen.verificacion_padding_card_horizontal)
    val paddingCardV       = dimensionResource(R.dimen.verificacion_padding_card_vertical)
    val alturaBoton        = dimensionResource(R.dimen.verificacion_altura_boton)
    val campoAncho         = dimensionResource(R.dimen.verificacion_campo_ancho)
    val campoAlto          = dimensionResource(R.dimen.verificacion_campo_alto)
    val tamanoCirculoGrande = dimensionResource(R.dimen.inicio_sesion_circulo_grande)
    val tamanoCirculoMediano = dimensionResource(R.dimen.inicio_sesion_circulo_mediano)
    val tamanoCirculoPequeno = dimensionResource(R.dimen.inicio_sesion_circulo_pequeno)
    val anchoMaximo        = tamano.anchoMaximoContenido

    var segundosRestantes by remember { mutableStateOf(300) }
    var expirado by remember { mutableStateOf(false) }

    LaunchedEffect(estadoUi.verificado) {
        if (estadoUi.verificado) {
            navController.navigate(VoxTaskScreen.Inicio.name) {
                popUpTo(VoxTaskScreen.Verificacion.name) { inclusive = true }
            }
        }
    }

    LaunchedEffect(estadoUi.errorEnvio) {
        if (estadoUi.errorEnvio) {
            expirado = true
        }
    }

    LaunchedEffect(expirado && !estadoUi.errorEnvio) {
        if (!expirado && !estadoUi.errorEnvio) {
            segundosRestantes = 300
            while (segundosRestantes > 0) {
                delay(1000L)
                segundosRestantes--
            }
            expirado = true
        }
    }

    LaunchedEffect(estadoUi.mensajeError) {
        estadoUi.mensajeError?.let { resId ->
            snackbarHostState.showSnackbar(
                message = contexto.getString(resId),
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(estadoUi.errorDinamico) {
        if (estadoUi.errorDinamico.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = estadoUi.errorDinamico,
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }

    val minutos = segundosRestantes / 60
    val segundos = segundosRestantes % 60
    val tiempoTexto = "%02d:%02d".format(minutos, segundos)
    val colorContador = if (segundosRestantes <= 60) Color(0xFFE53935) else MaterialTheme.colorScheme.primary

    val focusRequesters = remember { List(5) { FocusRequester() } }
    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = espaciado.xl)
                .zIndex(10f)
        )

        if (esLandscape) {
            // ── Landscape: solo círculo arriba-izquierda y abajo-derecha ─────
            Box(
                modifier = Modifier
                    .size(tamanoCirculoGrande)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(tamanoCirculoGrande)
                    .align(Alignment.BottomEnd)
                    .offset(x = 80.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            // ── Portrait: círculos originales ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(tamanoCirculoGrande)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(tamanoCirculoMediano)
                    .offset(x = 270.dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .blur(2.dp)
            )
            Box(
                modifier = Modifier
                    .size(tamanoCirculoGrande)
                    .offset(x = 160.dp, y = 620.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(tamanoCirculoPequeno)
                    .offset(x = (-40).dp, y = 700.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .blur(1.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = paddingHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(espaciado.xl))

            // Limita el ancho en tabletas y plegables
            val modificadorCard = if (anchoMaximo != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.widthIn(max = anchoMaximo).fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }

            Card(
                modifier = modificadorCard,
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = paddingCardH, vertical = paddingCardV),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = tamano.textoTitulo,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(espaciado.s))

                    Text(
                        text = stringResource(R.string.txt_verificar_correo),
                        fontSize = tamano.textoBody,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(espaciado.s))

                    Text(
                        text = stringResource(R.string.txt_codigo_enviado_a),
                        fontSize = tamano.textoBody,
                        color = TextoGris
                    )

                    if (estadoUi.email.isNotEmpty()) {
                        Text(
                            text = estadoUi.email,
                            fontSize = tamano.textoBody,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(espaciado.l))

                    if (estadoUi.cargando) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(espaciado.s))
                        Text(
                            text = stringResource(R.string.txt_enviando_codigo),
                            fontSize = tamano.textoBody,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (estadoUi.errorEnvio) {
                        Text(
                            text = stringResource(R.string.txt_error_envio_codigo),
                            fontSize = tamano.textoBody,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE53935)
                        )
                    } else {
                        Text(
                            text = if (expirado) stringResource(R.string.txt_codigo_expirado)
                            else stringResource(R.string.txt_expira_en, tiempoTexto),
                            fontSize = tamano.textoBody,
                            fontWeight = FontWeight.Medium,
                            color = colorContador
                        )
                    }

                    Spacer(modifier = Modifier.height(espaciado.xl))

                    // Campos del código
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 5) {
                            OutlinedTextField(
                                value = codigo[i],
                                onValueChange = { value ->
                                    val nuevo = value.uppercase()
                                        .filter { it.isLetterOrDigit() }
                                        .take(1)
                                    val lista = codigo.toMutableList()
                                    lista[i] = nuevo
                                    codigo = lista
                                    if (nuevo.isNotEmpty() && i < 4) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .size(width = campoAncho, height = campoAlto)
                                    .focusRequester(focusRequesters[i])
                                    .onKeyEvent { evento ->
                                        if (evento.key == Key.Backspace &&
                                            evento.type == KeyEventType.KeyDown &&
                                            codigo[i].isEmpty() && i > 0
                                        ) {
                                            val lista = codigo.toMutableList()
                                            lista[i - 1] = ""
                                            codigo = lista
                                            focusRequesters[i - 1].requestFocus()
                                            true
                                        } else false
                                    },
                                singleLine = true,
                                enabled = !expirado && !estadoUi.cargando && !estadoUi.errorEnvio,
                                textStyle = TextStyle(
                                    fontSize = tamano.textoBody,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(espaciado.xl))

                    Button(
                        onClick = { viewModel.verificarCodigo(codigoUnido) },
                        enabled = codigoUnido.length == 5 && !expirado && !estadoUi.cargando && !estadoUi.errorEnvio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alturaBoton),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_verificar_codigo),
                            fontSize = tamano.textoBody,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (expirado || estadoUi.errorEnvio) {
                        Spacer(modifier = Modifier.height(espaciado.m))
                        TextButton(onClick = {
                            codigo = List(5) { "" }
                            expirado = false
                            segundosRestantes = 300
                            viewModel.enviarCodigo(contexto)
                            focusRequesters[0].requestFocus()
                        }) {
                            Text(
                                text = stringResource(R.string.btn_reenviar_codigo),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = tamano.textoBody,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(espaciado.xl))
        }
    }
}