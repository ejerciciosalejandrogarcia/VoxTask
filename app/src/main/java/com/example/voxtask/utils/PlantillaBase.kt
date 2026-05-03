package com.example.voxtask.utils

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.voxtask.R
import com.example.voxtask.VoxTaskScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantillaBase(
    viewModel: PlantillaBaseViewModel,
    navController: NavController,
    mostrarBotonInfo: Boolean = true,
    mostrarBotonSalir: Boolean = true,
    mostrarBotonAjustes: Boolean = true,
    mostrarBotonHome: Boolean = true,
    textoInformacion: String? = null,
    onTextoReconocido: (String) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val coroutineScope = rememberCoroutineScope()
    val (vozState, iniciarEscucha) = rememberVozATexto()

    LaunchedEffect(vozState.textoReconocido) {
        if (vozState.textoReconocido.isNotEmpty()) {
            onTextoReconocido(vozState.textoReconocido)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        viewModel.fondoPersonalizadoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Scaffold(
            containerColor = if (viewModel.fondoPersonalizadoUri != null) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        if (mostrarBotonInfo && !textoInformacion.isNullOrEmpty()) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    TextoAVoz.hablar(contexto, textoInformacion!!)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Información",
                                    tint = Color.White
                                )
                            }
                        }
                        if (mostrarBotonSalir) {
                            IconButton(
                                onClick = {
                                    viewModel.cerrarSesion(actividad, actividad) { }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    if (mostrarBotonAjustes) {
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(VoxTaskScreen.Ajustes.name)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.btn_ajustes),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            },
                            selected = false,
                            onClick = { }
                        )
                    }

                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (vozState.isListening) Color.Red else Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { iniciarEscucha() },
                                    enabled = !vozState.isListening
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = stringResource(R.string.btn_hablar),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        },
                        selected = false,
                        onClick = { iniciarEscucha() },
                        enabled = !vozState.isListening
                    )

                    if (mostrarBotonHome) {
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = {
                                        navController.navigate(VoxTaskScreen.Inicio.name)
                                    }) {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = stringResource(R.string.btn_inicio),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            },
                            selected = false,
                            onClick = {
                                navController.navigate(VoxTaskScreen.Inicio.name)
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                content(paddingValues)
            }
        }
    }
}