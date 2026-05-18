package com.example.voxtask.ui.screens.Inicio_Sesion

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import com.example.voxtask.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.theme.*
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun InicioSesionScreen(
    alIniciarSesionExitosamente: () -> Unit,
    alNavegarARegistro: (String) -> Unit = {},
    alPulsarGoogle: () -> Unit,
    viewModel: InicioSesionViewModel = viewModel()
) {
    val estadoUi by viewModel.estadoUi.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos
    val paddingHorizontalCard = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.xl      // 24 dp
        TamanioPantalla.MEDIO     -> 36.dp
        TamanioPantalla.EXPANDIDO -> 48.dp
    }
    val paddingVerticalCard = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.xl      // 24 dp
        TamanioPantalla.MEDIO     -> 36.dp
        TamanioPantalla.EXPANDIDO -> 48.dp
    }
    val alturaBotonPrincipal = when (tamano) {
        TamanioPantalla.COMPACTO  -> 54.dp
        TamanioPantalla.MEDIO     -> 58.dp
        TamanioPantalla.EXPANDIDO -> 64.dp
    }
    val alturaBotonGoogle = when (tamano) {
        TamanioPantalla.COMPACTO  -> 40.dp
        TamanioPantalla.MEDIO     -> 46.dp
        TamanioPantalla.EXPANDIDO -> 52.dp
    }

    // Nuevo: tamaño de los círculos decorativos adaptativo
    val tamanoCirculoGrande = when (tamano) {
        TamanioPantalla.COMPACTO  -> 280.dp
        TamanioPantalla.MEDIO     -> 340.dp
        TamanioPantalla.EXPANDIDO -> 400.dp
    }
    val tamanoCirculoMediano = when (tamano) {
        TamanioPantalla.COMPACTO  -> 160.dp
        TamanioPantalla.MEDIO     -> 200.dp
        TamanioPantalla.EXPANDIDO -> 240.dp
    }
    val tamanoCirculoPequeno = when (tamano) {
        TamanioPantalla.COMPACTO  -> 140.dp
        TamanioPantalla.MEDIO     -> 180.dp
        TamanioPantalla.EXPANDIDO -> 220.dp
    }

    // Nuevo: ancho máximo de la card para tabletas y plegables
    val anchoMaximoCard = tamano.anchoMaximoContenido

    LaunchedEffect(estadoUi.inicioSesionExitoso) {
        if (estadoUi.inicioSesionExitoso) {
            alIniciarSesionExitosamente()
            viewModel.limpiarEstadoInicioSesion()
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
                .padding(top = espaciado.xl)           // antes: 50.dp
                .zIndex(10f)
        )

        // Círculo grande superior izquierda
        Box(
            modifier = Modifier
                .size(tamanoCirculoGrande)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Círculo mediano superior derecha
        Box(
            modifier = Modifier
                .size(tamanoCirculoMediano)
                .offset(x = 270.dp, y = 40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .blur(2.dp)
        )

        // Círculo grande inferior derecha
        Box(
            modifier = Modifier
                .size(tamanoCirculoGrande)
                .offset(x = 160.dp, y = 620.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Círculo pequeño inferior izquierda
        Box(
            modifier = Modifier
                .size(tamanoCirculoPequeno)
                .offset(x = (-40).dp, y = 700.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .blur(1.dp)
        )

        // Nuevo: scroll para teclado abierto y pantallas pequeñas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = paddingHorizontalCard),  // antes: 36.dp fijo
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(espaciado.xl))

            // Nuevo: limita el ancho en tabletas y plegables, centrado automático
            val modificadorCard = if (anchoMaximoCard != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.widthIn(max = anchoMaximoCard).fillMaxWidth()
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
                        .padding(horizontal = paddingHorizontalCard, vertical = paddingVerticalCard), // antes: 28.dp, 36.dp
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(espaciado.s))    // antes: 8.dp

                    // Título
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = tamano.textoTitulo,                 // antes: 34.sp fijo
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(espaciado.xl))   // antes: 26.dp

                    // Campo nombre de usuario
                    OutlinedTextField(
                        value = estadoUi.nombreUsuario,
                        onValueChange = { viewModel.alCambiarNombreUsuario(it) },
                        label = { Text(stringResource(R.string.campo_nombre_usuario)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.nombreUsuario.isNotEmpty()) {
                                IconButton(onClick = { viewModel.alCambiarNombreUsuario("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
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

                    Spacer(modifier = Modifier.height(espaciado.m))    // antes: 14.dp

                    // Campo contraseña
                    OutlinedTextField(
                        value = estadoUi.contrasena,
                        onValueChange = { viewModel.alCambiarContrasena(it) },
                        label = { Text(stringResource(R.string.campo_contrasenia)) },
                        singleLine = true,
                        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    imageVector = if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(R.string.icono_mostrar_contrasenia),
                                    tint = TextoGris
                                )
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

                    // Mensaje de error
                    LaunchedEffect(estadoUi.mensajeError) {
                        estadoUi.mensajeError?.let { resId ->
                            snackbarHostState.showSnackbar(
                                message = contexto.getString(resId),
                                duration = SnackbarDuration.Short
                            )
                            viewModel.limpiarError()
                        }
                    }

                    Spacer(modifier = Modifier.height(espaciado.xl))   // antes: 28.dp

                    // Botón iniciar sesión
                    Button(
                        onClick = { viewModel.iniciarSesion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alturaBotonPrincipal),             // antes: 54.dp fijo
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_iniciar_sesion),
                            fontSize = tamano.textoBody,               // antes: 16.sp fijo
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp

                    // Divisor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(
                            text = "  " + stringResource(R.string.txt_o) + "  ",
                            color = TextoGris,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }

                    Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp

                    // Botón iniciar sesión con Google
                    OutlinedButton(
                        onClick = alPulsarGoogle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alturaBotonGoogle),                // antes: 40.dp fijo
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, Color(0xFF747775)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F1F1F)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Logo de Google",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(espaciado.s))  // antes: 8.dp
                            Text(
                                text = stringResource(R.string.txt_iniciar_google),
                                fontSize = tamano.textoBody,            // antes: 14.sp fijo
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(espaciado.l))    // antes: 16.dp

                    // Botón redirigir a registro
                    TextButton(onClick = { alNavegarARegistro(VoxTaskScreen.Registro_Usuario.name) }) {
                        Text(
                            text = stringResource(R.string.txt_pregunta),
                            color = TextoGris,
                            fontSize = tamano.textoBody                // antes: 13.sp fijo
                        )
                        Text(
                            text = " " + stringResource(R.string.txt_respuesta),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = tamano.textoBody,               // antes: 13.sp fijo
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón redirigir a cambiar contraseña
                    TextButton(
                        onClick = { alNavegarARegistro(VoxTaskScreen.CambiarContrasenia.name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.txt_pregunta_contrasenia),
                                color = TextoGris,
                                fontSize = tamano.textoBody            // antes: 13.sp fijo
                            )
                            Spacer(modifier = Modifier.height(espaciado.xs)) // antes: 4.dp
                            Text(
                                text = stringResource(R.string.txt_respuesta_contrasenia),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = tamano.textoBody,           // antes: 13.sp fijo
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