package com.example.voxtask.ui.screens.Verificacion

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
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

    var segundosRestantes by remember { mutableStateOf(300) }
    var expirado by remember { mutableStateOf(false) }

    LaunchedEffect(estadoUi.verificado) {
        if (estadoUi.verificado) {
            navController.navigate(VoxTaskScreen.Inicio.name) {
                popUpTo(VoxTaskScreen.Verificacion.name) { inclusive = true }
            }
        }
    }

    LaunchedEffect(expirado) {
        if (!expirado) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .zIndex(10f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.txt_verificar_correo),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.txt_codigo_enviado_a),
                        fontSize = 13.sp,
                        color = TextoGris
                    )

                    if (estadoUi.email.isNotEmpty()) {
                        Text(
                            text = estadoUi.email,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (estadoUi.cargando) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.txt_enviando_codigo), fontSize = 12.sp, color = TextoGris)
                    } else {
                        Text(
                            text = if (expirado) stringResource(R.string.txt_codigo_expirado)
                            else stringResource(R.string.txt_expira_en, tiempoTexto),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorContador
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                                    .size(width = 48.dp, height = 56.dp)
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
                                enabled = !expirado && !estadoUi.cargando,
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
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
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.verificarCodigo(codigoUnido) },
                        enabled = codigoUnido.length == 5 && !expirado && !estadoUi.cargando,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_verificar_codigo),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (expirado) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = {
                            codigo = List(5) { "" }
                            expirado = false
                            viewModel.enviarCodigo(contexto)
                            focusRequesters[0].requestFocus()
                        }) {
                            Text(
                                text = stringResource(R.string.btn_reenviar_codigo),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}