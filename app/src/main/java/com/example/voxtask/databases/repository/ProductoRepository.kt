package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.ProductoDao
import com.example.voxtask.databases.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
/**
 * Repositorio encargado de las operaciones para los productos
 */
class ProductoRepository : ProductoDao {

    /** Variables */
    private val db = FirebaseFirestore.getInstance()

    /**
     * Agrega un nuevo producto al subcoleccion 'lista_compra' de un usuario en especifico
     */
    override suspend fun agregar(usuarioId: String, producto: Producto) {
        db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .document(producto.id)
            .set(producto)
            .await()
    }
    /**
     * Elimina un producto al subcoleccion 'lista_compra' de un usuario en especifico
     */
    override suspend fun eliminar(usuarioId: String, productoId: String) {
        db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .document(productoId)
            .delete()
            .await()
    }

    /**
     * Obtiene una lista de productos de un usuario en especifico
     */
    override suspend fun obtenerPorUsuario(usuarioId: String): List<Producto> {
        return db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .get()
            .await()
            .documents.map { doc ->
                Producto(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: ""
                )
            }
    }
}