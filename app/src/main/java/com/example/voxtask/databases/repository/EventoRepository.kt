package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.EventoDao
import com.example.voxtask.databases.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventoRepository : EventoDao {

    //Variables
    private val db = FirebaseFirestore.getInstance()
    private val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    //Funciones
    override suspend fun agregar(usuarioId: String, evento: Evento) {
        val ref = db.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .document()

        val eventoConId = evento.copy(id = ref.id)
        ref.set(eventoConId).await()
    }

    override suspend fun eliminar(usuarioId: String, eventoId: String) {
        db.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .document(eventoId)
            .delete()
            .await()
    }

    override suspend fun obtenerPorFecha(
        usuarioId: String,
        dia: Int, mes: Int, anio: Int
    ): List<Evento> {
        return db.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .whereEqualTo("dia", dia)
            .whereEqualTo("mes", mes)
            .whereEqualTo("anio", anio)
            .get()
            .await()
            .toObjects(Evento::class.java)
    }

    override suspend fun obtenerTodos(usuarioId: String): List<Evento> {
        return db.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .get()
            .await()
            .toObjects(Evento::class.java)
    }
}