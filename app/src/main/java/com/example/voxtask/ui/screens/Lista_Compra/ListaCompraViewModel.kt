package com.example.voxtask.ui.screens.Lista_Compra

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Producto
import com.example.voxtask.databases.repository.ProductoRepository
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class ListaCompraViewModel : ViewModel() {

    /** Variables */
    private val repository = ProductoRepository()
    val productos = mutableStateListOf<Producto>()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Carga los productos de el usuario logueado */
    init {
        cargarProductos()
    }

    /** Permite cargar los productos de la lista de la compra del usuario  */
    private fun cargarProductos() {
        if (usuarioId.isEmpty()) return
        viewModelScope.launch {
            try {
                val listaProductos = repository.obtenerPorUsuario(usuarioId)
                productos.clear()
                productos.addAll(listaProductos)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Permite recibir el texto transformado por voz y lo convierte a minusculas y elimina los espacions */
    fun onTextoRecibido(texto: String) {
        procesarComando(texto.lowercase().trim())
    }

    /** Permite procesar el texto mediante la voz y ejecutar las acciones programadas dependiendo del idioma seleccionado */
    private fun procesarComando(texto: String) {
        val idioma = TextoAVoz.localeActual.language

        val prefijosEliminar = when (idioma) {
            "en" -> listOf("delete ", "remove ", "erase ")
            "fr" -> listOf("supprime ", "enlève ", "efface ")
            "de" -> listOf("lösche ", "entferne ", "streiche ")
            "it" -> listOf("elimina ", "rimuovi ", "cancella ")
            "pt" -> listOf("elimina ", "remove ", "apaga ")
            else -> listOf("elimina ", "quita ", "borra ")
        }

        val prefijosAgregar = when (idioma) {
            "en" -> listOf("add ", "put ", "include ")
            "fr" -> listOf("ajoute ", "mets ", "inclus ")
            "de" -> listOf("füge ", "hinzufügen ", "ergänze ")
            "it" -> listOf("aggiungi ", "metti ", "inserisci ")
            "pt" -> listOf("adiciona ", "põe ", "inclui ")
            else -> listOf("agrega ", "añade ", "pon ")
        }

        val prefijoEliminar = prefijosEliminar.find { texto.startsWith(it) }
        val prefijoAgregar = prefijosAgregar.find { texto.startsWith(it) }

        when {
            prefijoEliminar != null -> {
                val nombre = texto.removePrefix(prefijoEliminar).trim()
                if (nombre.isNotEmpty()) eliminarProductoPorNombre(nombre)
            }
            prefijoAgregar != null -> {
                val nombre = texto.removePrefix(prefijoAgregar).trim()
                if (nombre.isNotEmpty()) agregarProducto(nombre)
            }
            texto.isNotEmpty() -> {
                agregarProducto(texto.trim())
            }
        }
    }
    /** Permite agregar un producto en la lista de la compra */
    private fun agregarProducto(nombre: String) {
        viewModelScope.launch {
            val producto = Producto(
                id = UUID.randomUUID().toString(),
                nombre = nombre
            )
            repository.agregar(usuarioId, producto)
            productos.add(producto)
        }
    }

    /** Permite eliminar un producto en la lista de la compra (Se elimina por boton)*/
    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.eliminar(usuarioId, producto.id)
            productos.remove(producto)
        }
    }

    /** Permite eliminar un producto en la lista de la compra (Se elimina por voz mediante el nombre del producto)*/
    private fun eliminarProductoPorNombre(nombre: String) {
        viewModelScope.launch {
            val producto = productos.find { it.nombre.equals(nombre, ignoreCase = true) }
            if (producto != null) {
                repository.eliminar(usuarioId, producto.id)
                productos.remove(producto)
            }
        }
    }

}