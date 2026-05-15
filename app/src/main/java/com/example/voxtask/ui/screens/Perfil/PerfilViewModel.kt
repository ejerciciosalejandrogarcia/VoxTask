package com.example.voxtask.ui.screens.Perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PerfilUiState(
    val nombre: String = "",
    val nombreUsuario: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val avatarSeleccionado: String = "",
    val cargando: Boolean = false,
    val mensajeError: Int? = null,
    val modoEdicion: Boolean = false,
    val operacionExitosa:Boolean = false
)
fun nombreAvatar(nombre: String): Int? = when (nombre) {
    "tigre"   -> R.drawable.tigre
    "leon"      -> R.drawable.leon
    "zorro"      -> R.drawable.zorro
    "astronauta" -> R.drawable.astronauta
    else         -> null
}

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _estadoUi = MutableStateFlow(PerfilUiState())
    val estadoUi: StateFlow<PerfilUiState> = _estadoUi.asStateFlow()

    val avatarOpciones = listOf("tigre", "leon", "zorro", "astronauta")

    init {
        cargarPerfil()
    }

    fun alCambiarNombre(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombre = valor)
    }
    fun alCambiarNombreUsuario(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombreUsuario = valor)
    }
    fun alCambiarPrimerApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(primerApellido = valor)
    }
    fun alCambiarSegundoApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(segundoApellido = valor)
    }
    fun conmutarModoEdicion() {
        _estadoUi.value = _estadoUi.value.copy(modoEdicion = !_estadoUi.value.modoEdicion)
    }

    private fun cargarPerfil() {
        val idUsuario = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(cargando = true)
            try {
                val documento = firestore.collection("usuarios").document(idUsuario).get().await()
                if (documento.exists()) {
                    _estadoUi.value = _estadoUi.value.copy(
                        nombre = documento.getString("nombre") ?: "",
                        nombreUsuario = documento.getString("nombre_usuario") ?: "",
                        primerApellido = documento.getString("primer_apellido") ?: "",
                        segundoApellido = documento.getString("segundo_apellido") ?: "",
                        avatarSeleccionado = documento.getString("avatar") ?: ""
                    )
                }
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_cargar_datos)
            } finally {
                _estadoUi.value = _estadoUi.value.copy(cargando = false)
            }
        }
    }

    fun guardarPerfil() {
        val uiState = _estadoUi.value
        val nombre = uiState.nombre.trim()
        val nombreUsuario = uiState.nombreUsuario.trim()
        val primerApellido = uiState.primerApellido.trim()
        val segundoApellido = uiState.segundoApellido.trim()

        val regexNombre = Regex("^[a-záéíóúàèìòùäëïöüñçâêîôûãõ]+$", RegexOption.IGNORE_CASE)
        val regexNombreUsuario = Regex("^[a-zA-Z0-9]+$")

        when {
            nombre.isBlank() || nombreUsuario.isBlank() || primerApellido.isBlank() || segundoApellido.isBlank() -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.err_campos_vacios)
                return
            }
            !regexNombreUsuario.matches(nombreUsuario) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.err_nombre_usuario_invalido)
                return
            }
            !regexNombre.matches(nombre) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_nombre_invalido)
                return
            }
            !regexNombre.matches(primerApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_primer_apellido_invalido)
                return
            }
            !regexNombre.matches(segundoApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_segundo_apellido_invalido)
                return
            }
            else -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = null)

                viewModelScope.launch {
                    _estadoUi.value = _estadoUi.value.copy(cargando = true)
                    try {
                        val idUsuario = auth.currentUser?.uid ?: return@launch
                        firestore.collection("usuarios").document(idUsuario).update(
                            mapOf(
                                "nombre" to nombre,
                                "nombre_usuario" to nombreUsuario,
                                "primer_apellido" to primerApellido,
                                "segundo_apellido" to segundoApellido,
                                "avatar" to uiState.avatarSeleccionado
                            )
                        ).await()

                        _estadoUi.value = _estadoUi.value.copy(
                            operacionExitosa = true,
                            modoEdicion = false,
                            mensajeError = R.string.msg_perfil_actualizado
                        )
                    } catch (e: Exception) {
                        _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_guardar_perfil)
                    } finally {
                        _estadoUi.value = _estadoUi.value.copy(cargando = false)
                    }
                }
            }
        }
    }

    fun seleccionarYGuardarAvatar(nombreAvatar: String) {
        _estadoUi.value = _estadoUi.value.copy(avatarSeleccionado = nombreAvatar)
        viewModelScope.launch {
            try {
                val idUsuario = auth.currentUser?.uid ?: return@launch
                firestore.collection("usuarios").document(idUsuario)
                    .update("avatar", nombreAvatar).await()
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.msg_avatar_actualizado)
            } catch (e: Exception) {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.error_actualizar_avatar)
            }
        }
    }

    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null)
    }
}