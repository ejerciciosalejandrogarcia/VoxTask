package com.example.voxtask.ui.screens.Inicio

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdePrimario
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
    plantillaBaseViewModel: PlantillaBaseViewModel
) {
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val coroutineScope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    var mostrarTextos by remember { mutableStateOf(false) }
    val texto by viewModel.textoReconocido.collectAsState()

    // Estado dinámico para lista y eventos
    var tieneLista by remember { mutableStateOf(false) }
    var tieneEventos by remember { mutableStateOf(false) }

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

        // Comprobar si tiene lista de la compra
        if (uid != null) {
            try {
                val lista = FirebaseFirestore.getInstance()
                    .collection("usuarios").document(uid)
                    .collection("lista_compra").limit(1).get().await()
                tieneLista = !lista.isEmpty
            } catch (e: Exception) { }

            try {
                val eventos = FirebaseFirestore.getInstance()
                    .collection("usuarios").document(uid)
                    .collection("eventos").limit(1).get().await()
                tieneEventos = !eventos.isEmpty
            } catch (e: Exception) { }
        }

        if (!viewModel.bienvenidaDada) {
            TextoAVoz.hablar(contexto, "Hola, ¿cómo estás? $nombre. ¿Qué te gustaría que hiciera por ti?")
            viewModel.bienvenidaDada = true
        }

        mostrarTextos = true
        TextoAVoz.hablar(contexto, "Elige una de las siguientes opciones y cuando estés listo mantén el botón de hablar")
    }

    PlantillaBase(
        viewModel = plantillaBaseViewModel,
        navController = navController,
        mostrarBotonInfo = true,
        mostrarBotonSalir = true,
        mostrarBotonAjustes = true,
        mostrarBotonHome = false,
        textoInformacion = "Elige una de las siguientes opciones y cuando estés listo mantén el botón de hablar",
        onTextoReconocido = { textoRecibido ->
            viewModel.onTextoRecibido(textoRecibido)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center  // ← Centra todo el contenido
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // ← Solo ocupa el alto necesario
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center  // ← Centra verticalmente
            ) {
                AnimatedVisibility(visible = mostrarTextos) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TarjetaMenu(
                            icono = Icons.Default.Email,
                            titulo = stringResource(R.string.txt_correo),
                            subtexto = "Ver mi correo",
                            onClick = { navController.navigate("correo") }
                        )

                        TarjetaMenu(
                            icono = Icons.Default.DateRange,
                            titulo = stringResource(R.string.txt_recordatorio),
                            subtexto = if (tieneEventos) "Ver mis recordatorios" else "Crear recordatorio",
                            onClick = { navController.navigate("recordatorio") }
                        )

                        TarjetaMenu(
                            icono = Icons.Default.ShoppingCart,
                            titulo = stringResource(R.string.txt_lista_compra),
                            subtexto = if (tieneLista) "Ver mi lista de la compra" else "Crear lista de la compra",
                            onClick = { navController.navigate("listacompra") }
                        )

                        TarjetaMenu(
                            icono = Icons.Default.Add,
                            titulo = stringResource(R.string.txt_contador),
                            subtexto = "Crear contador",
                            onClick = { navController.navigate("contador") }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaMenu(
    icono: ImageVector,
    titulo: String,
    subtexto: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = VerdePrimario,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}