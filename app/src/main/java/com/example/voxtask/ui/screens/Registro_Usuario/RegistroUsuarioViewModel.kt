package com.example.voxtask.ui.screens.Registro_Usuario

import android.util.Patterns
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.example.voxtask.R
import com.example.voxtask.databases.repository.UsuarioRepository
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.databases.dao.UsuarioDao
import kotlinx.coroutines.launch
import com.example.voxtask.databases.network.BienvenidaRequest
import com.example.voxtask.databases.network.N8nClient

data class RegistrarUsuarioUiState(
    val nombreUsuario: String = "",
    val nombre: String = "",
    val primer_apellido: String = "",
    val segundo_apellido: String = "",
    val fecha_nacimiento: String = "",
    val correo_electronico: String = "",
    val contrasenia: String = "",
    val registroUsuarioExitoso: Boolean = false,
    val mensajeError: Int? = null,
    val detalleError: Int? = null
)

class RegistroUsuarioViewModel(
    private val repositorio: UsuarioDao = UsuarioRepository()
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RegistrarUsuarioUiState())
    val estadoUi: StateFlow<RegistrarUsuarioUiState> = _estadoUi.asStateFlow()

    fun alCambiarNombreUsuario(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombreUsuario = valor)
    }
    fun alCambiarNombre(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombre = valor)
    }
    fun alCambiarPrimerApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(primer_apellido = valor)
    }
    fun alCambiarSegundoApellido(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(segundo_apellido = valor)
    }
    fun alCambiarFechaNacimiento(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(fecha_nacimiento = valor)
    }
    fun alCambiarCorreoElectronico(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(correo_electronico = valor)
    }
    fun alCambiarContrasenia(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(contrasenia = valor)
    }

    private fun setError(detalle: Int) {
        _estadoUi.value = _estadoUi.value.copy(
            mensajeError = R.string.txt_error,
            detalleError = detalle
        )
    }

    fun registrarUsuario() {
        val nombreUsuario       = _estadoUi.value.nombreUsuario.trim()
        val nombre              = _estadoUi.value.nombre.trim()
        val primerApellido      = _estadoUi.value.primer_apellido.trim()
        val segundoApellido     = _estadoUi.value.segundo_apellido.trim()
        val fechaNacimiento     = _estadoUi.value.fecha_nacimiento.trim()
        val correoElectronico   = _estadoUi.value.correo_electronico.trim()
        val contrasenia         = _estadoUi.value.contrasenia.trim()

        val regexNombre         = Regex("^[a-záéíóúàèìòùäëïöüñçâêîôûãõ]+$", RegexOption.IGNORE_CASE)
        val regexContrasenia    = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$")
        val regexNombreUsuario  = Regex("^[a-zA-Z0-9]+$")

        when {
            nombreUsuario.isBlank() ||
                    nombre.isBlank() ||
                    primerApellido.isBlank() ||
                    segundoApellido.isBlank() ||
                    fechaNacimiento.isBlank() ||
                    correoElectronico.isBlank() ||
                    contrasenia.isBlank() -> {
                setError(R.string.error_campos_vacios_registro)
                return
            }
            !regexNombreUsuario.matches(nombreUsuario) -> {
                setError(R.string.error_nombre_usuario_invalido_registro)
                return
            }
            !regexNombre.matches(nombre) -> {
                setError(R.string.error_nombre_invalido_registro)
                return
            }
            !regexNombre.matches(primerApellido) -> {
                setError(R.string.error_primer_apellido_invalido_registro)
                return
            }
            !regexNombre.matches(segundoApellido) -> {
                setError(R.string.error_segundo_apellido_invalido_registro)
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(correoElectronico).matches() -> {
                setError(R.string.error_correo_invalido)
                return
            }
            !regexContrasenia.matches(contrasenia) -> {
                setError(R.string.error_contrasenia_debil_registro)
                return
            }
            else -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = null, detalleError = null)

                viewModelScope.launch {
                    val usuario = Usuario(
                        nombre_usuario      = nombreUsuario,
                        nombre              = nombre,
                        primer_apellido     = primerApellido,
                        segundo_apellido    = segundoApellido,
                        fecha_nacimiento    = fechaNacimiento,
                        correo_electronico  = correoElectronico,
                        contrasenia         = contrasenia
                    )
                    val resultado = repositorio.registrarUsuario(usuario)

                    if (resultado.isSuccess) {
                        enviarCorreoBienvenida(correoElectronico)
                        _estadoUi.value = _estadoUi.value.copy(registroUsuarioExitoso = true)
                    } else {
                        setError(R.string.error_registro_fallido)
                    }
                }
            }
        }
    }

    suspend fun enviarCorreoBienvenida(email: String) {
        try {
            val response = N8nClient.api.enviarCorreoBienvenida(BienvenidaRequest(email))
            if (response.isSuccessful) {
                println("Correo de bienvenida enviado")
            } else {
                println("Error enviando correo")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null, detalleError = null)
    }

    fun limpiarEstadoRegistro() {
        _estadoUi.value = _estadoUi.value.copy(registroUsuarioExitoso = false)
    }
}