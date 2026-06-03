package com.example.voxtask.databases.dao

import com.example.voxtask.databases.model.Evento
/**
 * Interfaz que define las operaciones sobre los eventos del calendario
 */
interface EventoDao {
    suspend fun agregar(usuarioId: String, evento: Evento)
    suspend fun eliminar(usuarioId: String, eventoId: String)
    suspend fun obtenerPorFecha(usuarioId: String, dia: Int, mes: Int, anio: Int): List<Evento>
    suspend fun obtenerTodos(usuarioId: String): List<Evento>
}