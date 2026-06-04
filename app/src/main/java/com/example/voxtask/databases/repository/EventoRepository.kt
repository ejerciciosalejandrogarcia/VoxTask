package com.example.voxtask.databases.repository

import com.example.voxtask.databases.dao.EventoDao
import com.example.voxtask.databases.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
/**
 * Repositorio encargado de las operaciones para los eventos
 */
class EventoRepository : EventoDao {

    /** Variables */
    private val bd = FirebaseFirestore.getInstance()

    /**
     * Agrega un nuevo evento al subcoleccion evento de un usuario en especifico
     */
    override suspend fun agregar(usuarioId: String, evento: Evento) {
        val referencia = bd.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .document()

        val eventoConId = evento.copy(id = referencia.id)
        referencia.set(eventoConId).await()
    }

    /**
     * Elimina un evento al subcoleccion evento de un usuario en especifico
     */
    override suspend fun eliminar(usuarioId: String, eventoId: String) {
        bd.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .document(eventoId)
            .delete()
            .await()
    }

    /**
     * Obtiene una lista de eventos filtrado por fecha
     */
    override suspend fun obtenerPorFecha(
        usuarioId: String,
        dia: Int, mes: Int, anio: Int
    ): List<Evento> {
        return bd.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .whereEqualTo("dia", dia)
            .whereEqualTo("mes", mes)
            .whereEqualTo("anio", anio)
            .get()
            .await()
            .toObjects(Evento::class.java)
    }

    /**
     * Obtiene todos los eventos registrados por un usuario en especifico
     */
    override suspend fun obtenerTodos(usuarioId: String): List<Evento> {
        return bd.collection("usuarios")
            .document(usuarioId)
            .collection("eventos")
            .get()
            .await()
            .toObjects(Evento::class.java)
    }
}