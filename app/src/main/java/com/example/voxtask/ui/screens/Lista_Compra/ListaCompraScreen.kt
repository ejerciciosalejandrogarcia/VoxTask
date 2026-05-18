package com.example.voxtask.ui.screens.Lista_Compra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.TextoAVoz
import com.example.voxtask.R
import com.example.voxtask.databases.model.Producto
import com.example.voxtask.utils.PlantillaBaseViewModel

@Composable
fun ListaCompraScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: ListaCompraViewModel,
    navController: NavController
) {
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    var itemAEliminar by remember { mutableStateOf<Producto?>(null) }

    // Valores adaptativos
    val paddingContenido = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.l       // 16 dp
        TamanioPantalla.MEDIO     -> espaciado.xl      // 32 dp
        TamanioPantalla.EXPANDIDO -> 48.dp
    }
    val paddingFilaHorizontal = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.l       // 16 dp
        TamanioPantalla.MEDIO     -> espaciado.xl      // 24 dp
        TamanioPantalla.EXPANDIDO -> 28.dp
    }
    val paddingFilaVertical = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.m       // 12 dp
        TamanioPantalla.MEDIO     -> espaciado.l       // 16 dp
        TamanioPantalla.EXPANDIDO -> espaciado.xl      // 24 dp
    }

    // Nuevo: ancho máximo del contenido para tabletas y plegables
    val anchoMaximoContenido = tamano.anchoMaximoContenido

    // Mensaje de confirmación a la hora de eliminar un producto
    itemAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            title = { Text(stringResource(R.string.txt_titulo_mensaje_confirmacion_producto)) },
            text = { Text(stringResource(R.string.txt_mensaje_confirmacion_producto, producto.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(producto)
                    itemAEliminar = null
                }) {
                    Text(stringResource(R.string.btn_eliminar_producto), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemAEliminar = null }) {
                    Text(stringResource(R.string.btn_cancelar_producto))
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        TextoAVoz.hablar(contexto, "Lista de compra. Di un producto para agregarlo, o di elimina y el producto para borrarlo.")
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        textoInformacion = stringResource(R.string.txt_info_lista_compra),
        navController = navController,
        onTextoReconocido = { texto -> viewModel.onTextoRecibido(texto) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(paddingContenido),             // antes: 16.dp fijo
            contentAlignment = Alignment.TopCenter
        ) {
            // Nuevo: limita el ancho en tabletas y plegables, centrado automático
            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximoContenido)
                    .fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            }

            Column(modifier = modificadorContenido) {
                // Cuando no haya ningún producto en la lista
                if (viewModel.productos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.txt_sin_producto_lista_compra),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontSize = tamano.textoBody
                        )
                    }
                    // Si hay productos en la lista
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(espaciado.s) // antes: 8.dp
                    ) {
                        items(viewModel.productos) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    .padding(
                                        horizontal = paddingFilaHorizontal,  // antes: 16.dp fijo
                                        vertical = paddingFilaVertical       // antes: 12.dp fijo
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.nombre.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = tamano.textoBody              // nuevo: texto adaptativo
                                )
                                IconButton(onClick = { itemAEliminar = item }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.icono_eliminar_producto),
                                        tint = Color.Red.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}