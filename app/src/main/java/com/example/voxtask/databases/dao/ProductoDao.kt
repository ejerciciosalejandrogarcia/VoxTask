package com.example.voxtask.databases.dao

import com.example.voxtask.databases.model.Producto

/**
 * Interfaz que define las operaciones sobre los productos de la lista de la compra
 */
interface ProductoDao {
    suspend fun agregar(usuarioId: String, producto: Producto)
    suspend fun eliminar(usuarioId: String, productoId: String)
    suspend fun obtenerPorUsuario(usuarioId: String): List<Producto>
}