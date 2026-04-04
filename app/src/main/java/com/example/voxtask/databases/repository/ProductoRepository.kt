package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.ProductoDao
import com.example.voxtask.databases.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository : ProductoDao {

    //Variables
    private val db = FirebaseFirestore.getInstance()

    //Funciones
    override suspend fun agregar(usuarioId: String, producto: Producto) {
        db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .add(producto)
            .await()
    }

    override suspend fun eliminar(usuarioId: String, productoId: String) {
        db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .document(productoId)
            .delete()
            .await()
    }

    override suspend fun obtenerPorUsuario(usuarioId: String): List<Producto> {
        return db.collection("usuarios")
            .document(usuarioId)
            .collection("lista_compra")
            .get()
            .await()
            .toObjects(Producto::class.java)
    }
}