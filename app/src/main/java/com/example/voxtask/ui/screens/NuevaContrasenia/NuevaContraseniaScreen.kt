package com.example.voxtask.ui.screens.NuevaContrasenia

// Cambia este import en NuevaContraseniaScreen.kt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContraseniaViewModel
import com.example.voxtask.ui.theme.*

@Composable
fun NuevaContraseniaScreen(
    navController: NavController,
    viewModel: CambiarContraseniaViewModel = viewModel(),
    oobCode: String = ""
) {

    val estado by viewModel.estadoNueva.collectAsState()
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize().background(FondoBlanco)) {
        // Círculos decorativos
        Box(modifier = Modifier.size(280.dp).offset(x = (-80).dp, y = (-60).dp)
            .clip(CircleShape).background(Brush.radialGradient(listOf(VerdeClaro, VerdePrimario))))
        Box(modifier = Modifier.size(160.dp).offset(x = 270.dp, y = 40.dp)
            .clip(CircleShape).background(Brush.radialGradient(listOf(VerdeMenta, VerdeClaro))).blur(2.dp))
        Box(modifier = Modifier.size(300.dp).offset(x = 160.dp, y = 620.dp)
            .clip(CircleShape).background(Brush.radialGradient(listOf(VerdeClaro, VerdePrimario))))
        Box(modifier = Modifier.size(140.dp).offset(x = (-40).dp, y = 700.dp)
            .clip(CircleShape).background(Brush.radialGradient(listOf(VerdeMenta, VerdeClaro))).blur(1.dp))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        // Cargando
                        estado.cargando -> {
                            CircularProgressIndicator(color = VerdePrimario, modifier = Modifier.size(56.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Guardando contraseña...", color = Color.Gray, fontSize = 14.sp)
                        }

                        // Éxito
                        estado.cambioExitoso -> {
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }

                            AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
                                Box(
                                    modifier = Modifier.size(80.dp).clip(CircleShape).background(VerdePrimario),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("¡Contraseña cambiada!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = VerdePrimario)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ya puedes iniciar sesión con tu nueva contraseña.",
                                fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = {
                                    navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario)
                            ) {
                                Text("Ir al inicio de sesión", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Formulario
                        else -> {
                            Text("Nueva contraseña", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                                color = VerdePrimario, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Introduce tu nueva contraseña.", fontSize = 13.sp,
                                color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(28.dp))

                            OutlinedTextField(
                                value = estado.nuevaContrasena,
                                onValueChange = { viewModel.alCambiarNuevaContrasena(it) },
                                label = { Text("Nueva contraseña") },
                                singleLine = true,
                                visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { verNueva = !verNueva }) {
                                        Icon(if (verNueva) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null, tint = TextoGris)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VerdeClaro, unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedLabelColor = VerdeClaro, unfocusedLabelColor = VerdeClaro, cursorColor = VerdeClaro
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = estado.confirmarContrasena,
                                onValueChange = { viewModel.alCambiarConfirmarContrasena(it) },
                                label = { Text("Confirmar contraseña") },
                                singleLine = true,
                                visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { verConfirmar = !verConfirmar }) {
                                        Icon(if (verConfirmar) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null, tint = TextoGris)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VerdeClaro, unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedLabelColor = VerdeClaro, unfocusedLabelColor = VerdeClaro, cursorColor = VerdeClaro
                                )
                            )
                            if (estado.mensajeError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(estado.mensajeError, color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = { viewModel.guardarNuevaContrasena(oobCode) },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario),
                                enabled = !estado.cargando
                            ) {
                                Text("Guardar contraseña", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

}