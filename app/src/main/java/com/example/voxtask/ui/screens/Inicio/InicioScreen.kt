package com.example.voxtask.ui.screens.Inicio

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.TextoAVoz
import com.example.voxtask.utils.rememberVozATexto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    navController: NavController
) {
    //Variables
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val coroutineScope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    var mostrarTextos by remember { mutableStateOf(false) }
    val (vozState, iniciarEscucha) = rememberVozATexto()
    val texto by viewModel.textoReconocido.collectAsState()


    // Texto a voz a view Modal
    LaunchedEffect(vozState.textoReconocido) {
        if (vozState.textoReconocido.isNotEmpty()) {
            viewModel.onTextoRecibido(vozState.textoReconocido)
        }
    }

    //Obtener informacion del usuario logueado
    LaunchedEffect(uid) {
        val nombre = if (uid != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .get()
                    .await()
                val usuarioObj = doc.toObject(Usuario::class.java)
                usuarioObj?.nombre ?: "Usuario"
            } catch (e: Exception) {
                "Usuario"
            }
        } else {
            "Usuario"
        }

        TextoAVoz.hablar(contexto, "Hola, ¿cómo estás? $nombre. ¿Qué te gustaría que hiciera por ti?")
        mostrarTextos = true
        TextoAVoz.hablar(contexto, "Elige una de las siguientes opciones y cuando estés listo mantén el botón de hablar")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdePrimario,
                    titleContentColor = Color.White
                ),
                actions = {
                    //Boton traductor
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                TextoAVoz.hablar(contexto, "Abriendo la opción de traducir")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate, // Ícono de traducir
                            contentDescription = stringResource(R.string.btn_traducir),
                            tint = Color.White
                        )
                    }

                    //Boton cerrar sesion
                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion(contexto,actividad) {
                                navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VerdePrimario,
                contentColor = Color.White
            ) {
                // Botón ajustes
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
                                    coroutineScope.launch {
                                        TextoAVoz.hablar(contexto, "Abriendo ajustes")
                                    }
                                    navController.navigate("ajustes")

                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.btn_ajustes),
                                    tint = VerdePrimario,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            TextoAVoz.hablar(contexto, "Abriendo ajustes")
                        }
                    }
                )
                // Botón micrófono
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
                                    tint = VerdePrimario,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    },
                    selected = false,
                    onClick = { iniciarEscucha() },
                    enabled = !vozState.isListening
                )
                NavigationBarItem(
                    icon = { },
                    selected = false,
                    onClick = { },
                    enabled = false
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = mostrarTextos) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.txt_contador),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.txt_correo),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.txt_recordatorio),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.txt_lista_compra),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                    }
                }
            }
        }

    }
}