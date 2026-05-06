package com.example.voxtask.ui.screens.Cambiar_contrasenia


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.theme.*

@Composable
fun CambiarContrasenaScreen(
    navController: NavController,
    viewModel: CambiarContraseniaViewModel = viewModel()
) {

    val estadoUi by viewModel.estadoUi.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Círculo grande superior izquierda — igual que InicioSesion
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Círculo mediano superior derecha
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 270.dp, y = 40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .blur(2.dp)
        )

        // Círculo grande inferior derecha
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 160.dp, y = 620.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Círculo pequeño inferior izquierda
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-40).dp, y = 700.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
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

                    if (estadoUi.correoEnviado) {
                        // ── Pantalla de éxito ──
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Correo enviado",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Revisa tu bandeja de entrada en\n${estadoUi.email}\ny sigue las instrucciones para restablecer tu contraseña.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = "Volver al inicio de sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.reiniciar() }) {
                            Text(
                                text = "Enviar de nuevo",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // ── Formulario ──
                        Text(
                            text = "Recuperar contraseña",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Introduce tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedTextField(
                            value = estadoUi.email,
                            onValueChange = { viewModel.alCambiarEmail(it) },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.email.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarEmail("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Limpiar",
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

                        Button(
                            onClick = { viewModel.enviarCorreoRecuperacion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
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
                                    text = "Enviar correo de recuperación",
                                    fontSize = 15.sp,
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
