package com.example.voxtask.ui.screens.Perfil

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.R

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PerfilScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: PerfilViewModel,
    navController: NavController
) {
    // Observar el estado del ViewModel (StateFlow)
    val uiState by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarSelector by remember { mutableStateOf(false) }

    // Manejo de mensajes de error o éxito
    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let { resId ->
            snackbarHostState.showSnackbar(
                message = contexto.getString(resId),
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController
    ) { paddingValues ->

        // Box es el contenedor principal que permite superponer elementos (como el Snackbar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal en una columna
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // --- SECCIÓN AVATAR ---
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { mostrarSelector = true },
                    contentAlignment = Alignment.Center
                ) {
                    val drawableId = nombreAvatar(uiState.avatarSeleccionado)
                    if (drawableId != null) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = stringResource(R.string.content_desc_avatar),
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.content_desc_avatar),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "@${uiState.nombreUsuario}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.cargando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    OutlinedTextField(
                        value = uiState.nombreUsuario,
                        onValueChange = { viewModel.alCambiarNombreUsuario(it) },
                        label = { Text(stringResource(R.string.hint_nombre_usuario)) },
                        enabled = uiState.modoEdicion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.alCambiarNombre(it) },
                        label = { Text(stringResource(R.string.hint_nombre)) },
                        enabled = uiState.modoEdicion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.primerApellido,
                        onValueChange = { viewModel.alCambiarPrimerApellido(it) },
                        label = { Text(stringResource(R.string.hint_primer_apellido)) },
                        enabled = uiState.modoEdicion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.segundoApellido,
                        onValueChange = { viewModel.alCambiarSegundoApellido(it) },
                        label = { Text(stringResource(R.string.hint_segundo_apellido)) },
                        enabled = uiState.modoEdicion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                        )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BOTONES DE ACCIÓN ---
                    Button(
                        onClick = {
                            if (uiState.modoEdicion) {
                                viewModel.guardarPerfil()
                            } else {
                                viewModel.conmutarModoEdicion()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.modoEdicion) stringResource(R.string.btn_guardar_cambios) else stringResource(R.string.btn_editar_perfil)
                        )
                    }

                    if (uiState.modoEdicion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.conmutarModoEdicion() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.btn_cancelar))
                        }
                    }
                }
            }

            if (mostrarSelector) {
                AlertDialog(
                    onDismissRequest = { mostrarSelector = false },
                    title = { Text(stringResource(R.string.txt_titulo_selector_avatar)) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            viewModel.avatarOpciones.forEach { nombre ->
                                val drawableId = nombreAvatar(nombre)
                                if (drawableId != null) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = nombre,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (uiState.avatarSeleccionado == nombre) 3.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.seleccionarYGuardarAvatar(nombre)
                                                mostrarSelector = false
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { mostrarSelector = false }) {
                            Text(stringResource(R.string.btn_cerrar_avatar))
                        }
                    }
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .zIndex(1f)
            )
        }
    }
}