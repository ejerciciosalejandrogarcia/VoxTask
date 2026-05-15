package com.example.voxtask.ui.screens.NuevaContrasenia

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContraseniaViewModel
import com.example.voxtask.ui.theme.*
import com.example.voxtask.R

@Composable
fun NuevaContraseniaScreen(
    navController: NavController,
    viewModel: CambiarContraseniaViewModel = viewModel(),
    oobCode: String = ""
) {

    val estado by viewModel.estadoNueva.collectAsState()
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val estadoNueva by viewModel.estadoNueva.collectAsState()
    val contexto = LocalContext.current

    LaunchedEffect(estadoNueva.mensajeError) {
        estadoNueva.mensajeError?.let { resId ->
            snackbarHostState.showSnackbar(
                message = contexto.getString(resId),
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarErrorNueva()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .zIndex(10f)
        )

        // Círculos decorativos
        Box(modifier = Modifier.size(280.dp).offset(x = (-80).dp, y = (-60).dp)
            .clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.size(160.dp).offset(x = 270.dp, y = 40.dp)
            .clip(CircleShape).background(MaterialTheme.colorScheme.primary).blur(2.dp))
        Box(modifier = Modifier.size(300.dp).offset(x = 160.dp, y = 620.dp)
            .clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.size(140.dp).offset(x = (-40).dp, y = 700.dp)
            .clip(CircleShape).background(MaterialTheme.colorScheme.primary).blur(1.dp))

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
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.txt_cargando_contrasena), color = Color.Gray, fontSize = 14.sp)
                        }

                        // Éxito
                        estado.cambioExitoso -> {
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }

                            AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
                                Box(
                                    modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(R.string.txt_contrasena_cambiada), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.txt_contrasena_cambiada_descripcion),
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
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.btn_ir_inicio_sesion), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Formulario
                        else -> {
                            Text(stringResource(R.string.txt_titulo_nueva_contrasena), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.txt_descripcion_nueva_contrasena), fontSize = 13.sp,
                                color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(28.dp))

                            OutlinedTextField(
                                value = estado.nuevaContrasena,
                                onValueChange = { viewModel.alCambiarNuevaContrasena(it) },
                                label = { Text(stringResource(R.string.hint_nueva_contrasena)) },
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = TextoOscuro,
                                    focusedTextColor = TextoOscuro,
                                    unfocusedTextColor = TextoOscuro,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = estado.confirmarContrasena,
                                onValueChange = { viewModel.alCambiarConfirmarContrasena(it) },
                                label = { Text(stringResource(R.string.hint_confirmar_contrasena)) },
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = TextoOscuro,
                                    focusedTextColor = TextoOscuro,
                                    unfocusedTextColor = TextoOscuro,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = { viewModel.guardarNuevaContrasena(oobCode) },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = !estado.cargando
                            ) {
                                Text(stringResource(R.string.btn_guardar_contrasena), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

}