package com.example.voxtask.ui.screens.Cambiar_contrasenia

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.ui.theme.*
import com.example.voxtask.R
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.textoTitulo
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.anchoMaximoContenido

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CambiarContrasenaScreen(
    navController: NavController,
    viewModel: CambiarContraseniaViewModel = viewModel()
) {
    val context = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    val estadoUi by viewModel.estadoUi.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Valores adaptativos
    val paddingHorizontalCard = dimensionResource(R.dimen.cambiar_contrasena_padding_card_horizontal)
    val paddingVerticalCard = dimensionResource(R.dimen.cambiar_contrasena_padding_card_vertical)
    val alturaBoton = dimensionResource(R.dimen.cambiar_contrasena_altura_boton)
    val tamanoIconoEmail = dimensionResource(R.dimen.cambiar_contrasena_icono_email)
    val tamanoCirculoGrande = dimensionResource(R.dimen.cambiar_contrasena_circulo_grande)
    val tamanoCirculoMediano = dimensionResource(R.dimen.cambiar_contrasena_circulo_mediano)
    val tamanoCirculoPequeno = dimensionResource(R.dimen.cambiar_contrasena_circulo_pequeno)
    val anchoMaximoCard = tamano.anchoMaximoContenido

    // Detectar orientación
    val configuracion = androidx.compose.ui.platform.LocalConfiguration.current
    val esLandscape = configuracion.screenWidthDp > configuracion.screenHeightDp

    LaunchedEffect(estadoUi.mensajeError) {
        if (estadoUi.mensajeError != 0) {
            snackbarHostState.showSnackbar(
                message = context.getString(estadoUi.mensajeError),
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.reiniciar()
        }
    }

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
                .padding(horizontal = paddingHorizontalCard),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val modificadorCard = if (anchoMaximoCard != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximoCard)
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }

            Card(
                modifier = modificadorCard,
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = paddingVerticalCard,
                            vertical = paddingVerticalCard
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (estadoUi.correoEnviado) {
                        // ── Pantalla de éxito ──
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(tamanoIconoEmail)
                        )
                        Spacer(modifier = Modifier.height(espaciado.l))
                        Text(
                            text = stringResource(R.string.instrucciones_restablecer_email_uno),
                            fontSize = tamano.textoTitulo,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(espaciado.m))
                        Text(
                            text = stringResource(
                                id = R.string.instrucciones_restablecer_email_dos,
                                estadoUi.email
                            ),
                            fontSize = tamano.textoBody,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(espaciado.xl))

                        TextButton(onClick = { viewModel.enviarCorreoRecuperacion() }) {
                            Text(
                                text = stringResource(R.string.btn_enviar_de_nuevo),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = tamano.textoBody,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // ── Formulario ──
                        Text(
                            text = stringResource(R.string.txt_titulo_recuperar_contrasenia),
                            fontSize = tamano.textoTitulo,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(espaciado.s))
                        Text(
                            text = stringResource(R.string.txt_titulo_recuperar_contrasenia_dos),
                            fontSize = tamano.textoBody,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(espaciado.xl))

                        OutlinedTextField(
                            value = estadoUi.email,
                            onValueChange = { viewModel.alCambiarEmail(it) },
                            label = { Text(stringResource(R.string.txt_placeholder_recuperar_contrasenia)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.email.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarEmail("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = stringResource(R.string.txt_limpiar),
                                            tint = TextoGris
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = TextoOscuro,
                                focusedTextColor = TextoOscuro,
                                unfocusedTextColor = TextoOscuro,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(espaciado.xl))

                        Button(
                            onClick = { viewModel.enviarCorreoRecuperacion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(alturaBoton),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            enabled = !estadoUi.cargando
                        ) {
                            if (estadoUi.cargando) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.btn_recuperar_contrasenia),
                                    fontSize = tamano.textoBody,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}