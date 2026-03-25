package com.example.voxtask.ui.screens.Inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdeClaro
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen() {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    var mostrarTextos by remember { mutableStateOf(false) }

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
                    //Boton ajustes
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                TextoAVoz.hablar(contexto, "Abriendo ajustes")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.btn_ajustes),
                            tint = Color.White
                        )
                    }
                    //Boton traducir
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // Aquí puedes llamar a tu función de traducción o TTS
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
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VerdePrimario,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.btn_inicio)) },
                    selected = true,
                    onClick = {
                        coroutineScope.launch {
                            TextoAVoz.hablar(contexto, "Inicio")
                        }
                        // Aquí navegaremos a la pantalla de inicio
                        // navController.navigate("inicio") { popUpTo("inicio") { inclusive = true } }
                    }
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

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    //Hacer otro dia
                                    //La logica para que cuando pulsemos el boton traducir lo que decimos a texto
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.btn_hablar)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}