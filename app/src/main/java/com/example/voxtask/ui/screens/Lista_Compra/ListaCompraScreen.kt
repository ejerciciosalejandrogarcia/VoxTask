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
import androidx.compose.ui.res.dimensionResource
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
/**
 * Pantalla principal
 */
@Composable
fun ListaCompraScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: ListaCompraViewModel,
    navController: NavController
) {
    /** Variables */
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    var elementoAEliminar by remember { mutableStateOf<Producto?>(null) }
    val paddingContenido = dimensionResource(R.dimen.lista_compra_padding_contenido)
    val paddingFilaHorizontal = dimensionResource(R.dimen.lista_compra_padding_fila_horizontal)
    val paddingFilaVertical = dimensionResource(R.dimen.lista_compra_padding_fila_vertical)
    val anchoMaximoContenido = tamano.anchoMaximoContenido

    /** Ventana emergente de la opcion 'eliminar producto' */
    elementoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { elementoAEliminar = null },
            title = { Text(stringResource(R.string.txt_titulo_mensaje_confirmacion_producto)) },
            text = { Text(stringResource(R.string.txt_mensaje_confirmacion_producto, producto.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(producto)
                    elementoAEliminar = null
                }) {
                    Text(stringResource(R.string.btn_eliminar_producto), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { elementoAEliminar = null }) {
                    Text(stringResource(R.string.btn_cancelar_producto))
                }
            }
        )
    }

    /**
     * Da intrucciones auditivas segun el idioma seleccionado
     */
    LaunchedEffect(Unit) {
        val idioma = TextoAVoz.localeActual.language
        val mensaje = when (idioma) {
            "en" -> "Shopping list. Say a product to add it, or say delete and the product to remove it."
            "fr" -> "Liste de courses. Dites un produit pour l'ajouter, ou dites supprimer et le produit pour le supprimer."
            "de" -> "Einkaufsliste. Sagen Sie ein Produkt, um es hinzuzufügen, oder sagen Sie löschen und das Produkt, um es zu entfernen."
            "it" -> "Lista della spesa. Di un prodotto per aggiungerlo, o di elimina e il prodotto per rimuoverlo."
            "pt" -> "Lista de compras. Diga um produto para adicioná-lo, ou diga eliminar e o produto para removê-lo."
            else -> "Lista de compra. Di un producto para agregarlo, o di elimina y el producto para borrarlo."
        }
        TextoAVoz.hablar(contexto, mensaje)
    }
    /** Lista de la compra */
    PlantillaBase(
        viewModel = viewModelPlantilla,
        textoInformacion = stringResource(R.string.txt_info_lista_compra),
        navController = navController,
        onTextoReconocido = { texto -> viewModel.onTextoRecibido(texto) }
    ) { valoresPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(valoresPadding)
                .padding(paddingContenido),
            contentAlignment = Alignment.TopCenter
        ) {
            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximoContenido)
                    .fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            }

            Column(    modifier = modificadorContenido
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
                .padding(espaciado.m)
            ) {
                /** Pantalla cuando no hay ningun producto registrado */
                if (viewModel.productos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.txt_sin_producto_lista_compra),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontSize = tamano.textoBody*1.3f
                        )
                    }
                    /** Pantalla cuando hay productos registrados */
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(espaciado.s)
                    ) {
                        items(viewModel.productos) { elemento ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(
                                        horizontal = paddingFilaHorizontal,
                                        vertical = paddingFilaVertical
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = elemento.nombre.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = tamano.textoBody
                                )
                                IconButton(onClick = { elementoAEliminar = elemento }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.icono_eliminar_producto),
                                        tint = MaterialTheme.colorScheme.primary
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