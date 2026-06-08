package com.example.voxtask.ui.screens.NuevaContrasenia

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContraseniaViewModel
import com.example.voxtask.ui.theme.*
import com.example.voxtask.R
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
/**
 * Pantalla principal
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun NuevaContraseniaScreen(
    navController: NavController,
    viewModel: CambiarContraseniaViewModel = viewModel(),
    oobCode: String = ""
) {
    /** Variables */
    val estado by viewModel.estadoNueva.collectAsState()
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }
    val estadoSnackbar = remember { SnackbarHostState() }
    val estadoNueva by viewModel.estadoNueva.collectAsState()
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val paddingHorizontalCard = dimensionResource(R.dimen.nueva_contrasenia_padding_horizontal)
    val paddingVerticalCard = dimensionResource(R.dimen.nueva_contrasenia_padding_vertical)
    val alturaBoton = dimensionResource(R.dimen.nueva_contrasenia_altura_boton)
    val tamanoIconoExito = dimensionResource(R.dimen.nueva_contrasenia_icono_exito)
    val tamanoCheckExito = dimensionResource(R.dimen.nueva_contrasenia_check_exito)
    val tamanoCirculoGrande = dimensionResource(R.dimen.nueva_contrasenia_circulo_grande)
    val tamanoCirculoMediano = dimensionResource(R.dimen.nueva_contrasenia_circulo_mediano)
    val tamanoCirculoPequeno = dimensionResource(R.dimen.nueva_contrasenia_circulo_pequeno)
    val anchoMaximoCard = tamano.anchoMaximoContenido
    val configuracion = LocalConfiguration.current
    val esLandscape = configuracion.screenWidthDp > configuracion.screenHeightDp

    /** Gestion del SnackBar */
    LaunchedEffect(estadoNueva.mensajeError) {
        estadoNueva.mensajeError?.let { idRecurso ->
            estadoSnackbar.showSnackbar(
                message = contexto.getString(R.string.txt_error) + " " + contexto.getString(idRecurso),
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarErrorNueva()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SnackbarHost(
            hostState = estadoSnackbar,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = espaciado.xl)
                .zIndex(10f)
        )

        /** Fondo de la aplicacion */
        if (esLandscape) {
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
        /** Formulario de nuevo cambio de contraseña */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = paddingHorizontalCard),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(espaciado.xl))

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
                        .padding(horizontal = paddingVerticalCard, vertical = paddingVerticalCard),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        /** Pantalla cuando se esta procesando el cambio de contraseña */
                        estado.cargando -> {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(espaciado.l))
                            Text(
                                stringResource(R.string.txt_cargando_contrasena),
                                color = Color.Gray,
                                fontSize = tamano.textoBody
                            )
                        }

                        /** Pantalla cuando se ha cambiado la contraseña exitosamente */
                        estado.cambioExitoso -> {
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }

                            AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
                                Box(
                                    modifier = Modifier
                                        .size(tamanoIconoExito)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(tamanoCheckExito)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(espaciado.xl))
                            Text(
                                stringResource(R.string.txt_contrasena_cambiada),
                                fontSize = tamano.textoTitulo,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(espaciado.s))
                            Text(
                                stringResource(R.string.txt_contrasena_cambiada_descripcion),
                                fontSize = tamano.textoBody,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(espaciado.xl))
                            Button(
                                onClick = {
                                    navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(alturaBoton),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    stringResource(R.string.btn_ir_inicio_sesion),
                                    fontSize = tamano.textoBody,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        /** Formulario para cambiar la contraseña */
                        else -> {
                            Text(
                                stringResource(R.string.txt_titulo_nueva_contrasena),
                                fontSize = tamano.textoTitulo,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(espaciado.s))
                            Text(
                                stringResource(R.string.txt_descripcion_nueva_contrasena),
                                fontSize = tamano.textoBody,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(espaciado.xl))

                            OutlinedTextField(
                                value = estado.nuevaContrasena,
                                onValueChange = { viewModel.alCambiarNuevaContrasena(it) },
                                label = { Text(stringResource(R.string.hint_nueva_contrasena)) },
                                singleLine = true,
                                visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { verNueva = !verNueva }) {
                                        Icon(
                                            if (verNueva) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
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
                            Spacer(modifier = Modifier.height(espaciado.m))
                            OutlinedTextField(
                                value = estado.confirmarContrasena,
                                onValueChange = { viewModel.alCambiarConfirmarContrasena(it) },
                                label = { Text(stringResource(R.string.hint_confirmar_contrasena)) },
                                singleLine = true,
                                visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { verConfirmar = !verConfirmar }) {
                                        Icon(
                                            if (verConfirmar) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
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
                            Spacer(modifier = Modifier.height(espaciado.xl))
                            Button(
                                onClick = { viewModel.guardarNuevaContrasena(oobCode) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(alturaBoton),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = !estado.cargando
                            ) {
                                Text(
                                    stringResource(R.string.btn_guardar_contrasena),
                                    fontSize = tamano.textoBody,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(espaciado.xl))
        }
    }
}