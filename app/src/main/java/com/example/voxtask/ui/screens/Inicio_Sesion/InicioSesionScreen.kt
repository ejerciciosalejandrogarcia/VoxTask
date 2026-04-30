package com.example.voxtask.ui.screens.Inicio_Sesion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.theme.*


@Composable
fun InicioSesionScreen(
    alIniciarSesionExitosamente: () -> Unit,
    alNavegarARegistro: (String) -> Unit = {},   // ← acepta String
    alPulsarGoogle: () -> Unit,
    viewModel: InicioSesionViewModel = viewModel()
) {
    val estadoUi by viewModel.estadoUi.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(estadoUi.inicioSesionExitoso) {
        if (estadoUi.inicioSesionExitoso) {
            alIniciarSesionExitosamente()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBlanco)
    ) {

        //Círculo grande superior izquierda
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VerdeClaro, VerdePrimario)
                    )
                )
        )

        //Círculo mediano superior derecha
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 270.dp, y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VerdeMenta, VerdeClaro)
                    )
                )
                .blur(2.dp)
        )

        //  Círculo grande inferior derecha
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 160.dp, y = 620.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VerdeClaro, VerdePrimario)
                    )
                )
        )

        //  Círculo pequeño inferior izquierda
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-40).dp, y = 700.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VerdeMenta, VerdeClaro)
                    )
                )
                .blur(1.dp)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Spacer(modifier = Modifier.height(8.dp))

                    // Título
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VerdePrimario,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))


                    Spacer(modifier = Modifier.height(32.dp))

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
                            focusedBorderColor = VerdeClaro,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = VerdeClaro,
                            unfocusedLabelColor = TextoOscuro,
                            focusedTextColor = TextoOscuro,

                            cursorColor = VerdeClaro
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                            focusedBorderColor = VerdeClaro,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = VerdeClaro,
                            unfocusedLabelColor = TextoOscuro,
                            focusedTextColor = TextoOscuro,

                            cursorColor = VerdeClaro
                        )
                        )

                    // Mensaje de error
                    if (estadoUi.mensajeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = estadoUi.mensajeError,
                            color = Color(0xFFE53935),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Botón iniciar sesión
                    Button(
                        onClick = { viewModel.iniciarSesion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdePrimario
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_iniciar_sesion),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divisor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = "  "+stringResource(R.string.txt_o)+"  ",
                            color = TextoGris,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Boton iniciar sesion google
                    OutlinedButton(
                        onClick = alPulsarGoogle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.txt_iniciar_google),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Boton para redireccionar a la pantalla de registro
                    TextButton(onClick = { alNavegarARegistro(VoxTaskScreen.Registro_Usuario.name) }) {
                        Text(
                            text = stringResource(R.string.txt_pregunta),
                            color = TextoGris,
                            fontSize = 13.sp
                        )
                        Text(
                            text = " "+stringResource(R.string.txt_respuesta),
                            color = VerdePrimario,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Botón para redireccionar a cambiar contraseña
                    TextButton(
                        onClick = { alNavegarARegistro(VoxTaskScreen.CambiarContrasenia.name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Usamos Column para que el texto aparezca uno sobre otro
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.txt_pregunta_contrasenia), // "¿No te acuerdas de la contraseña?"
                                color = TextoGris,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp)) // Un pequeño espacio entre líneas
                            Text(
                                text = stringResource(R.string.txt_respuesta_contrasenia), // "Restablecer contraseña"
                                color = VerdePrimario,
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