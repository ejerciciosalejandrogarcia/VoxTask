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

/**
 * Esqueleto de una pantalla
 */
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
    contenido: @Composable (PaddingValues) -> Unit
) {
    /** Variables */
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val alcanceCorrutina = rememberCoroutineScope()
    val (estadoVoz, iniciarEscucha) = rememberVozATexto()

    /**
     * Permite observar el texto reconocido por voz
     */
    LaunchedEffect(estadoVoz.textoReconocido) {
        if (estadoVoz.textoReconocido.isNotEmpty()) {
            onTextoReconocido(estadoVoz.textoReconocido)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        /**
         * Muestra el fondo personalizado seleccionado por el usuario
         */
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
                /**Cabecera de la plantilla */
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
                        /** Botones (Informacion y Cerrar sesion) */
                        if (mostrarBotonInfo && !textoInformacion.isNullOrEmpty()) {
                            IconButton(onClick = {
                                alcanceCorrutina.launch {
                                    TextoAVoz.hablar(contexto, textoInformacion!!)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = stringResource(R.string.desc_boton_informacion),
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
                                    contentDescription = stringResource(R.string.cerrar_sesion),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                /** Pie de la navegacion de la plantilla */
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    /** Botones de navegacion (Ajustes,Inicio y microfono) */
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
                                    .background(if (estadoVoz.escuchando) Color.Red else Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { iniciarEscucha() },
                                    enabled = !estadoVoz.escuchando
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
                        enabled = !estadoVoz.escuchando
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
        ) { valoresPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                contenido(valoresPadding)
            }
        }
    }
}