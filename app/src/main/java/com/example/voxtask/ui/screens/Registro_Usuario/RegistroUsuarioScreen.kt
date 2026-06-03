package com.example.voxtask.ui.screens.Registro_Usuario

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voxtask.ui.theme.*
import java.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun RegistroUsuarioScreen(
    alRegistroExitoso: () -> Unit,
    viewModel: RegistroUsuarioViewModel = viewModel()
) {
    val estadoUi by viewModel.estadoUi.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val calendario = Calendar.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos desde dimens.xml
    val paddingHorizontal    = dimensionResource(R.dimen.registro_padding_horizontal)
    val paddingCardH         = dimensionResource(R.dimen.registro_padding_card_horizontal)
    val paddingCardV         = dimensionResource(R.dimen.registro_padding_card_vertical)
    val alturaBoton          = dimensionResource(R.dimen.registro_altura_boton)
    val tamanoIconoExito     = dimensionResource(R.dimen.registro_icono_exito)
    val circuloGrande        = dimensionResource(R.dimen.registro_circulo_grande)
    val circuloMediano       = dimensionResource(R.dimen.registro_circulo_mediano)
    val circuloPequeno       = dimensionResource(R.dimen.registro_circulo_pequeno)
    val circuloInferior      = dimensionResource(R.dimen.registro_circulo_inferior)
    val espaciadoCampos      = dimensionResource(R.dimen.registro_espaciado_campos)
    val anchoMaximo          = tamano.anchoMaximoContenido

    // Detectar orientación
    val configuracion = androidx.compose.ui.platform.LocalConfiguration.current
    val esLandscape = configuracion.screenWidthDp > configuracion.screenHeightDp

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

    LaunchedEffect(estadoUi.mensajeError) {
        val prefijo = estadoUi.mensajeError?.let { contexto.getString(it) } ?: ""
        val detalle = estadoUi.detalleError?.let { contexto.getString(it) } ?: ""
        val mensaje = "$prefijo $detalle".trim()

        if (mensaje.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
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
                    .size(circuloGrande)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(circuloGrande)
                    .align(Alignment.BottomEnd)
                    .offset(x = 80.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            // ── Portrait: círculos originales ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(circuloGrande)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(circuloMediano)
                    .offset(x = 270.dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .blur(2.dp)
            )
            Box(
                modifier = Modifier
                    .size(circuloInferior)
                    .offset(x = 160.dp, y = 620.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(circuloPequeno)
                    .offset(x = (-40).dp, y = 700.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .blur(1.dp)
            )
        }

        // Scroll para teclado abierto y pantallas pequeñas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = paddingHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Limita el ancho en tabletas y plegables
            val modificadorCard = if (anchoMaximo != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.widthIn(max = anchoMaximo).fillMaxWidth()
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
                        .padding(horizontal = paddingCardH, vertical = paddingCardV),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (estadoUi.registroUsuarioExitoso) {

                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter = scaleIn() + fadeIn()
                        ) {
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
                                    modifier = Modifier.size(tamanoIconoExito * 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(espaciado.l))

                        Text(
                            text = stringResource(R.string.txt_cuenta_creada),
                            fontSize = tamano.textoTitulo,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(espaciado.s))

                        Text(
                            text = stringResource(R.string.txt_cuenta_creada_descripcion),
                            fontSize = tamano.textoBody,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(espaciado.xl))

                        Button(
                            onClick = { alRegistroExitoso() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(alturaBoton),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                stringResource(R.string.txt_ir_inicio_sesion),
                                fontSize = tamano.textoBody,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                    } else {

                        Spacer(modifier = Modifier.height(espaciado.s))

                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = tamano.textoTitulo*1.9f,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(espaciado.xl))

                        // Campo nombre de usuario
                        OutlinedTextField(
                            value = estadoUi.nombreUsuario,
                            onValueChange = { viewModel.alCambiarNombreUsuario(it) },
                            label = { Text(stringResource(R.string.campo_nombre_usuario)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.nombreUsuario.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarNombreUsuario("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icono_limpiar), tint = TextoOscuro)
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

                        // Campo nombre
                        OutlinedTextField(
                            value = estadoUi.nombre,
                            onValueChange = { viewModel.alCambiarNombre(it) },
                            label = { Text(stringResource(R.string.campo_nombre)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.nombre.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarNombre("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icono_limpiar), tint = TextoOscuro)
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

                        // Campo primer apellido
                        OutlinedTextField(
                            value = estadoUi.primer_apellido,
                            onValueChange = { viewModel.alCambiarPrimerApellido(it) },
                            label = { Text(stringResource(R.string.campo_primer_ap)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.primer_apellido.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarPrimerApellido("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icono_limpiar), tint = TextoOscuro)
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

                        // Campo segundo apellido
                        OutlinedTextField(
                            value = estadoUi.segundo_apellido,
                            onValueChange = { viewModel.alCambiarSegundoApellido(it) },
                            label = { Text(stringResource(R.string.campo_segundo_ap)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.segundo_apellido.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarSegundoApellido("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icono_limpiar), tint = TextoOscuro)
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

                        // Campo fecha nacimiento
                        OutlinedTextField(
                            value = estadoUi.fecha_nacimiento,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.hint_fecha_nacimiento)) },
                            trailingIcon = {
                                IconButton(onClick = { selectorFecha.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.content_desc_seleccionar_fecha))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectorFecha.show() },
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

                        // Campo correo electrónico
                        OutlinedTextField(
                            value = estadoUi.correo_electronico,
                            onValueChange = { viewModel.alCambiarCorreoElectronico(it) },
                            label = { Text(stringResource(R.string.campo_correo)) },
                            singleLine = true,
                            trailingIcon = {
                                if (estadoUi.correo_electronico.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.alCambiarCorreoElectronico("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icono_limpiar), tint = TextoOscuro)
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

                        Spacer(modifier = Modifier.height(espaciadoCampos))

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

                        Spacer(modifier = Modifier.height(espaciado.xl))

                        // Botón crear cuenta
                        Button(
                            onClick = { viewModel.registrarUsuario() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(alturaBoton),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_registrar_usuario),
                                fontSize = tamano.textoBody,
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