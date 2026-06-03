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
/**
 * Representa el estado de la UI
 */
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
/**
 * Permite mapear el nombre de un avatar a su foto,
 * devuelve null si el nombre proporcionado no tiene una imagen asociada
 */
fun nombreAvatar(nombre: String): Int? = when (nombre) {
    "tigre"   -> R.drawable.tigre
    "leon"      -> R.drawable.leon
    "zorro"      -> R.drawable.zorro
    "astronauta" -> R.drawable.astronauta
    else         -> null
}

class PerfilViewModel : ViewModel() {
    /** Variables */
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _estadoUi = MutableStateFlow(PerfilUiState())
    val estadoUi: StateFlow<PerfilUiState> = _estadoUi.asStateFlow()
    val avatarOpciones = listOf("tigre", "leon", "zorro", "astronauta")

    /** Carga los datos de el usuario logueado */
    init {
        cargarPerfil()
    }

    /** Actualiza el nombre del usuario en el estado */
    fun alCambiarNombre(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombre = valor)
    }
    /** Actualiza el nombre de usuario en el estado */
    fun alCambiarNombreUsuario(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombreUsuario = valor)
    }
    /** Actualiza el primer apellido del usuario en el estado */
    fun alCambiarPrimerApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(primerApellido = valor)
    }
    /** Actualiza el segundo apellido del usuario en el estado */
    fun alCambiarSegundoApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(segundoApellido = valor)
    }
    /** Actualiza el modo edicion en el estado */
    fun conmutarModoEdicion() {
        _estadoUi.value = _estadoUi.value.copy(modoEdicion = !_estadoUi.value.modoEdicion)
    }

    /** Permite obtener los datos del usuario logueado desde Firebase */
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
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.error_cargar_datos)
            } finally {
                _estadoUi.value = _estadoUi.value.copy(cargando = false)
            }
        }
    }

    /** Permite gestionar los posibles errores a la hora de guardar los datos y si es exitoso mostrara un mensaje de que se ha guardado los nuevos datos */
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
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_campos_vacios)
                return
            }
            !regexNombreUsuario.matches(nombreUsuario) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_nombre_usuario_invalido)
                return
            }
            !regexNombre.matches(nombre) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.error_nombre_invalido)
                return
            }
            !regexNombre.matches(primerApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.error_primer_apellido_invalido)
                return
            }
            !regexNombre.matches(segundoApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.error_segundo_apellido_invalido)
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

    /**
     * Permte actualizar el avatar del usuario tanto en el estado de la interfaz de usuario
     * como en Firestore
     */
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

    /**
     * Permite limpiar los mensajes de error de la pantalla 'Cambiar Contrasenia'
     */
    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null)
    }
}