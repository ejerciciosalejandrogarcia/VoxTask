package com.example.voxtask.ui.screens.Perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel

@Composable
fun PerfilScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: PerfilViewModel,
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra el mensaje de exito o error
    LaunchedEffect(viewModel.mensaje) {
        viewModel.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensaje = null
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // Avatar del usuario
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = VerdePrimario,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "@${viewModel.nombreUsuario}",
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdePrimario
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.cargando) {
                    CircularProgressIndicator(color = VerdePrimario)
                } else {
                    // Campo nombre
                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.nombre = it },
                        label = { Text("Nombre") },
                        enabled = viewModel.modoEdicion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo nombre de usuario
                    OutlinedTextField(
                        value = viewModel.nombreUsuario,
                        onValueChange = { viewModel.nombreUsuario = it },
                        label = { Text("Nombre de usuario") },
                        enabled = viewModel.modoEdicion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo primer apellido
                    OutlinedTextField(
                        value = viewModel.primerApellido,
                        onValueChange = { viewModel.primerApellido = it },
                        label = { Text("Primer apellido") },
                        enabled = viewModel.modoEdicion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo segundo apellido
                    OutlinedTextField(
                        value = viewModel.segundoApellido,
                        onValueChange = { viewModel.segundoApellido = it },
                        label = { Text("Segundo apellido") },
                        enabled = viewModel.modoEdicion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Boton editar o guardar segun el modo
                    Button(
                        onClick = {
                            if (viewModel.modoEdicion) {
                                viewModel.guardarPerfil()
                            } else {
                                viewModel.modoEdicion = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (viewModel.modoEdicion) "Guardar cambios" else "Editar perfil")
                    }

                    // Boton cancelar solo visible en modo edicion
                    if (viewModel.modoEdicion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.modoEdicion = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }

            // Snackbar para mostrar mensajes
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}