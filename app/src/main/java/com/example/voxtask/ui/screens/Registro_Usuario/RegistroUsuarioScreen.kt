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
import com.example.voxtask.ui.theme.*
import java.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RegistroUsuarioScreen(
    alRegistroExitoso: () -> Unit,
    viewModel: RegistroUsuarioViewModel = viewModel()
) {
    //Variables
    val estadoUi by viewModel.estadoUi.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val calendario = Calendar.getInstance()


    val selectorFecha = remember {
        DatePickerDialog(
            contexto,
            { _, year, month, dayOfMonth ->
                viewModel.alCambiarFechaNacimiento("$dayOfMonth/${month + 1}/$year")
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        //Círculo grande superior izquierda
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        //Círculo mediano superior derecha
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 270.dp, y = 40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .blur(2.dp)
        )

        //Círculo grande inferior derecha
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 160.dp, y = 620.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        //Círculo pequeño inferior izquierda
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

                    if (estadoUi.registroUsuarioExitoso) {

                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = scaleIn() + fadeIn()
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Cuenta creada con éxito",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ya puedes iniciar sesión con tus credenciales.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                alRegistroExitoso()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "Ir al inicio de sesión",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                    } else {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Título
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )



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
                                            tint = TextoOscuro
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Campo nombre
                        OutlinedTextField(
                            value = estadoUi.nombre,
                            onValueChange = { viewModel.alCambiarNombre(it) },
                            label = { Text(stringResource(R.string.campo_nombre)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.nombre.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarNombre("") }) {
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

                        // Campo primer apellido
                        OutlinedTextField(
                            value = estadoUi.primer_apellido,
                            onValueChange = { viewModel.alCambiarPrimerApellido(it) },
                            label = { Text(stringResource(R.string.campo_primer_ap)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.primer_apellido.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarPrimerApellido("") }) {
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


                        // Campo segundo apellido
                        OutlinedTextField(
                            value = estadoUi.segundo_apellido,
                            onValueChange = { viewModel.alCambiarSegundoApellido(it) },
                            label = { Text(stringResource(R.string.campo_segundo_ap)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.segundo_apellido.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarSegundoApellido("") }) {
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

                        // Campo fecha nacimiento
                        OutlinedTextField(
                            value = estadoUi.fecha_nacimiento,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha de nacimiento") },
                            trailingIcon = {
                                IconButton(onClick = { selectorFecha.show() }) {   // Icono para abrir el DatePicker
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Seleccionar fecha"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectorFecha.show() },  // También abre el selector al tocar el campo
                            shape = RoundedCornerShape(14.dp),
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

                        // Campo correo electronico
                        OutlinedTextField(
                            value = estadoUi.correo_electronico,
                            onValueChange = { viewModel.alCambiarCorreoElectronico(it) },
                            label = { Text(stringResource(R.string.campo_correo)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.correo_electronico.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarCorreoElectronico("") }) {
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

                        // Campo contraseña
                        OutlinedTextField(
                            value = estadoUi.contrasenia,
                            onValueChange = { viewModel.alCambiarContrasenia(it) },
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
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = TextoOscuro,
                                focusedTextColor = TextoOscuro,
                                unfocusedTextColor = TextoOscuro,
                                cursorColor = MaterialTheme.colorScheme.primary
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
                            onClick = { viewModel.registrarUsuario() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
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
}