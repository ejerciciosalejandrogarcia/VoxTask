package com.example.voxtask.ui.screens.Registro_Usuario

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource  // <- IMPORT MUY IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionUiState
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionViewModel
import com.example.voxtask.ui.theme.*
import java.util.Calendar


@Composable
fun RegistroUsuarioScreen(
    alRegistroExitoso: () -> Unit,
    modeloVista: RegistroUsuarioViewModel = viewModel()
) {
    val estadoUi by modeloVista.estadoUi.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val calendario = Calendar.getInstance()

    LaunchedEffect(estadoUi.registroUsuarioExitoso) {
        if (estadoUi.registroUsuarioExitoso) {
            alRegistroExitoso()
        }
    }

    val selectorFecha = remember {
        DatePickerDialog(
            contexto,
            { _, year, month, dayOfMonth ->
                modeloVista.alCambiarFechaNacimiento("$dayOfMonth/${month + 1}/$year")
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBlanco)
    ) {

        // ── Círculo grande superior izquierda ──
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

        // ── Círculo mediano superior derecha ──
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

        // ── Círculo grande inferior derecha ──
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

        // ── Círculo pequeño inferior izquierda ──
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



                    Spacer(modifier = Modifier.height(32.dp))

                    // Campo nombre de usuario
                    OutlinedTextField(
                        value = estadoUi.nombreUsuario,
                        onValueChange = { modeloVista.alCambiarNombreUsuario(it) },
                        label = { Text(stringResource(R.string.campo_nombre_usuario)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.nombreUsuario.isNotEmpty()) {
                                IconButton(onClick = { modeloVista.alCambiarNombreUsuario("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
                                        tint = TextoOscuro
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

                    // Campo nombre
                    OutlinedTextField(
                        value = estadoUi.nombre,
                        onValueChange = { modeloVista.alCambiarNombre(it) },
                        label = { Text(stringResource(R.string.campo_nombre)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.nombre.isNotEmpty()) {
                                IconButton(onClick = { modeloVista.alCambiarNombre("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
                                        tint = TextoOscuro
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

                    // Campo primer apellido
                    OutlinedTextField(
                        value = estadoUi.primer_apellido,
                        onValueChange = { modeloVista.alCambiarPrimerApellido(it) },
                        label = { Text(stringResource(R.string.campo_primer_ap)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.primer_apellido.isNotEmpty()) {
                                IconButton(onClick = { modeloVista.alCambiarPrimerApellido("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
                                        tint = TextoOscuro
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


                    // Campo segundo apellido
                    OutlinedTextField(
                        value = estadoUi.segundo_apellido,
                        onValueChange = { modeloVista.alCambiarSegundoApellido(it) },
                        label = { Text(stringResource(R.string.campo_segundo_ap)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.segundo_apellido.isNotEmpty()) {
                                IconButton(onClick = { modeloVista.alCambiarSegundoApellido("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
                                        tint = TextoOscuro
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

                    // Campo fecha nacimiento
                    OutlinedTextField(
                        value = estadoUi.fecha_nacimiento,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de nacimiento") },
                        trailingIcon = {
                            IconButton(onClick = { selectorFecha.show() }) {   // Icono para abrir el DatePicker
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectorFecha.show() },  // También abre el selector al tocar el campo
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerdeClaro,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = VerdeClaro,
                            unfocusedLabelColor = TextoOscuro,
                            focusedTextColor = TextoOscuro,
                            unfocusedTextColor = TextoOscuro,
                            cursorColor = VerdeClaro
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo correo electronico
                    OutlinedTextField(
                        value = estadoUi.correo_electronico,
                        onValueChange = { modeloVista.alCambiarCorreoElectronico(it) },
                        label = { Text(stringResource(R.string.campo_correo)) },
                        singleLine = true,
                        trailingIcon = {
                            if (estadoUi.correo_electronico.isNotEmpty()) {
                                IconButton(onClick = { modeloVista.alCambiarCorreoElectronico("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.icono_limpiar),
                                        tint = TextoOscuro
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
                        value = estadoUi.contrasenia,
                        onValueChange = { modeloVista.alCambiarContrasenia(it) },
                        label = { Text(stringResource(R.string.campo_contrasenia)) },
                        singleLine = true,
                        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    imageVector = if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(R.string.icono_mostrar_contrasenia),
                                    tint = TextoOscuro
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

                    // Mensaje de error del formulario
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

                    // Botón crear cuenta
                    Button(
                        onClick = { modeloVista.registrarUsuario() },
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
                            text = stringResource(R.string.btn_registrar_usuario),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}