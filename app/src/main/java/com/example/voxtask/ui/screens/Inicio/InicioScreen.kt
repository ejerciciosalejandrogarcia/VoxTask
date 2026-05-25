package com.example.voxtask.ui.screens.Inicio

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    navController: NavController,
    plantillaBaseViewModel: PlantillaBaseViewModel
) {
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid
    var mostrarTextos by remember { mutableStateOf(false) }

    var tieneLista by remember { mutableStateOf(false) }
    var tieneEventos by remember { mutableStateOf(false) }

    // Valores adaptativos
    val paddingHorizontal = dimensionResource(R.dimen.inicio_padding_horizontal)
    val paddingVertical = dimensionResource(R.dimen.inicio_padding_vertical)
    // Nuevo: ancho máximo del contenido para tabletas y plegables
    val anchoMaximoContenido = tamano.anchoMaximoContenido

    LaunchedEffect(uid) {
        val nombreUsuarioGenerico = contexto.getString(R.string.txt_usuario_generico)

        val nombre = if (uid != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .get()
                    .await()
                val usuarioObj = doc.toObject(Usuario::class.java)
                usuarioObj?.nombre ?: nombreUsuarioGenerico
            } catch (e: Exception) {
                nombreUsuarioGenerico
            }
        } else {
            nombreUsuarioGenerico
        }

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
            TextoAVoz.hablar(
                contexto,
                contexto.getString(R.string.txt_inicio_bienvenida, nombre)
            )
            viewModel.bienvenidaDada = true
        }

        mostrarTextos = true
        TextoAVoz.hablar(contexto, contexto.getString(R.string.txt_inicio_instrucciones))
    }

    PlantillaBase(
        viewModel = plantillaBaseViewModel,
        navController = navController,
        mostrarBotonInfo = true,
        mostrarBotonSalir = true,
        mostrarBotonAjustes = true,
        mostrarBotonHome = false,
        textoInformacion = stringResource(R.string.txt_inicio_instrucciones),
        onTextoReconocido = { textoRecibido ->
            viewModel.onTextoRecibido(textoRecibido)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = paddingHorizontal, vertical = paddingVertical), // antes: 20.dp, 24.dp
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(visible = mostrarTextos) {

                    // Nuevo: limita el ancho en tabletas y plegables, centrado automático
                    val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier
                            .widthIn(max = anchoMaximoContenido)
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(espaciado.m), // antes: 12.dp
                        modifier = modificadorContenido
                    ) {
                        TarjetaMenu(
                            icono = Icons.Default.Email,
                            titulo = stringResource(R.string.txt_correo),
                            subtexto = stringResource(R.string.txt_inicio_sub_correo)
                        )
                        TarjetaMenu(
                            icono = Icons.Default.DateRange,
                            titulo = stringResource(R.string.txt_recordatorio),
                            subtexto = if (tieneEventos)
                                stringResource(R.string.txt_inicio_sub_recordatorios_ver)
                            else
                                stringResource(R.string.txt_inicio_sub_recordatorios_crear)
                        )
                        TarjetaMenu(
                            icono = Icons.Default.ShoppingCart,
                            titulo = stringResource(R.string.txt_lista_compra),
                            subtexto = if (tieneLista)
                                stringResource(R.string.txt_inicio_sub_lista_ver)
                            else
                                stringResource(R.string.txt_inicio_sub_lista_crear)
                        )
                        TarjetaMenu(
                            icono = Icons.Default.Add,
                            titulo = stringResource(R.string.txt_contador),
                            subtexto = stringResource(R.string.txt_inicio_sub_contador)
                        )
                        TarjetaMenu(
                            icono = Icons.Default.Cloud,
                            titulo = stringResource(R.string.menu_clima_titulo),
                            subtexto = stringResource(R.string.menu_clima_subtexto)
                        )
                        Spacer(modifier = Modifier.height(espaciado.xl))        // antes: 32.dp
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
    subtexto: String
) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    // Valores adaptativos
    val paddingTarjeta = dimensionResource(R.dimen.inicio_padding_tarjeta)
    val tamanoIcono = dimensionResource(R.dimen.inicio_icono_tarjeta)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingTarjeta),                               // antes: 20.dp fijo
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(espaciado.l)  // antes: 16.dp
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(tamanoIcono)                   // antes: 32.dp fijo
            )
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = tamano.textoTitulo
                )
                Text(
                    text = subtexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = tamano.textoBody
                )
            }
        }
    }
}