package com.example.voxtask.ui.screens.Contador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.launch
import com.example.voxtask.utils.rememberVozATexto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContadorScreen(viewModel: ContadorViewModel,
                   Inicio: NavController
) {
    //Variables
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (vozState, iniciarEscucha) = rememberVozATexto()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    val mostrarContador = viewModel.mostrarContador

    LaunchedEffect(mostrarContador) {
        if (mostrarContador) {
            TextoAVoz.hablar(contexto, "Creando e iniciando contador.")
        }
    }
    //Obtengo informacion del usuario logueado
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

        TextoAVoz.hablar(contexto, "¿Okey $nombre cuanto tiempo quieres poner al contador?.")

    }



    // Envía el texto convertido por voz al ViewModel
    LaunchedEffect(vozState.textoReconocido) {
        if (vozState.textoReconocido.isNotEmpty()) {
            viewModel.onTextoRecibido(vozState.textoReconocido)
        }
    }

    //Plantilla de la app
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
                    IconButton(onClick = {
                        coroutineScope.launch { TextoAVoz.hablar(contexto, "Abriendo ajustes") }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.btn_ajustes), tint = Color.White)
                    }
                    IconButton(onClick = {
                        coroutineScope.launch { TextoAVoz.hablar(contexto, "Abriendo la opción de traducir") }
                    }) {
                        Icon(Icons.Default.Translate, contentDescription = stringResource(R.string.btn_traducir), tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = VerdePrimario, contentColor = Color.White) {
                NavigationBarItem(icon = { }, selected = false, onClick = { }, enabled = false)

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
                    icon = {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                coroutineScope.launch { TextoAVoz.hablar(contexto, "Inicio")}
                                Inicio.navigate("Inicio")
                            }) {
                                Icon(Icons.Default.Home, contentDescription = stringResource(R.string.btn_inicio), tint = VerdePrimario, modifier = Modifier.size(28.dp))
                            }
                        }
                    },
                    selected = false,
                    onClick = { coroutineScope.launch { TextoAVoz.hablar(contexto, "Inicio") } }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = viewModel.mostrarContador) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(VerdePrimario.copy(alpha = 0.1f))
                                .border(3.dp, VerdePrimario, CircleShape)
                        ) {
                            //Contador
                            Text(
                                text = viewModel.tiempoFormato,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = VerdePrimario
                            )
                        }
                    }
                }
            }
        }
    }
}