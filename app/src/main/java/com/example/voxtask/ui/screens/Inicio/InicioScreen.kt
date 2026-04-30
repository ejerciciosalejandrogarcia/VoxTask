package com.example.voxtask.ui.screens.Inicio

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    navController: NavController,
    plantillaBaseViewModel: PlantillaBaseViewModel // Recibir el ViewModel de la plantilla
) {
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val coroutineScope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    var mostrarTextos by remember { mutableStateOf(false) }
    val texto by viewModel.textoReconocido.collectAsState()

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

    // Usar la plantilla base sin el botón home
    PlantillaBase(
        viewModel = plantillaBaseViewModel,
        navController = navController,
        mostrarBotonInfo = true,
        mostrarBotonSalir = true,
        mostrarBotonAjustes = true,  // Mostrar botón ajustes
        mostrarBotonHome = false,     // NO mostrar botón home
        textoInformacion = "Elige una de las siguientes opciones y cuando estés listo mantén el botón de hablar",
        onTextoReconocido = { textoRecibido ->
            viewModel.onTextoRecibido(textoRecibido)
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