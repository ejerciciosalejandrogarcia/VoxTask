package com.example.voxtask.ui.screens.Lista_Compra

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Producto
import com.example.voxtask.databases.repository.ProductoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class ListaCompraViewModel : ViewModel() {

    //Variables
    private val repository = ProductoRepository()
    val productos = mutableStateListOf<Producto>()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    //Cargar los productos del usuario logueado
    init {
        cargarProductos()
    }

    //Funcion para cargar los productos de la lista de la compra del usuario
    private fun cargarProductos() {
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

    //Funcion que recibe el texto transformado por voz y lo convierte a minusculas y elimina espacios
    fun onTextoRecibido(texto: String) {
        procesarComando(texto.lowercase().trim())
    }

    //Funcion para procesar el texto mediante la voz y ejecutar las acciones programadas
    private fun procesarComando(texto: String) {
        when {
            texto.startsWith("elimina") ||
                    texto.startsWith("quita") ||
                    texto.startsWith("borra") -> {
                val nombre = texto
                    .removePrefix("elimina")
                    .removePrefix("quita")
                    .removePrefix("borra")
                    .trim()
                eliminarProductoPorNombre(nombre)
            }
            texto.isNotEmpty() -> {
                val nombre = texto
                    .removePrefix("agrega")
                    .removePrefix("añade")
                    .removePrefix("pon")
                    .trim()
                if (nombre.isNotEmpty()) {
                    agregarProducto(nombre)
                }
            }
        }
    }

    //Funcion para agregar un producto en la lista de la compra
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

    //Funcion para eliminar un producto en la lista de la compra (Se elimina por boton)
     fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.eliminar(usuarioId, producto.id)
            productos.remove(producto)
        }
    }

    // Funcion para eliminar un producto en la lista de la compra (Se elimina por voz mediante el nombre del producto)
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