package com.example.voxtask.ui.screens.Registro_Usuario

import android.util.Patterns
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
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
    val mensajeError: String = ""
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




    fun registrarUsuario() {
        val nombreUsuario = _estadoUi.value.nombreUsuario.trim()
        val nombre = _estadoUi.value.nombre.trim()
        val primerApellido = _estadoUi.value.primer_apellido.trim()
        val segundoApellido = _estadoUi.value.segundo_apellido.trim()
        val fechaNacimiento = _estadoUi.value.fecha_nacimiento.trim()
        val correoElectronico = _estadoUi.value.correo_electronico.trim()
        val contrasenia = _estadoUi.value.contrasenia.trim()
        val regexNombre = Regex("^[a-záéíóúàèìòùäëïöüñçâêîôûãõ]+$", RegexOption.IGNORE_CASE)
        val regexContrasenia = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$")
        val regexNombreUsuario = Regex("^[a-zA-Z0-9]+$")
        //Control de errores
        when {
            nombreUsuario.isBlank() ||
                    nombre.isBlank() ||
                    primerApellido.isBlank() ||
                    segundoApellido.isBlank() ||
                    fechaNacimiento.isBlank() ||
                    correoElectronico.isBlank() ||
                    contrasenia.isBlank() -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "Rellena todos los campos")
                return
            }

            !regexNombreUsuario.matches(nombreUsuario) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "El nombre de usuario solo puede contener letras y números")
                return
            }
            !regexNombre.matches(nombre) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "El nombre solo puede contener letras")
                return
            }

            !regexNombre.matches(primerApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "El primer apellido solo puede contener letras")
                return
            }

            !regexNombre.matches(segundoApellido) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "El segundo apellido solo puede contener letras")
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(correoElectronico).matches() -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "Correo electrónico no válido")
                return
            }

            !regexContrasenia.matches(contrasenia) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "La contraseña debe tener mínimo 9 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
                return
            }

            else -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "")

                viewModelScope.launch {
                    val usuario = Usuario(
                        nombre_usuario     = nombreUsuario,
                        nombre             = nombre,
                        primer_apellido    = primerApellido,
                        segundo_apellido   = segundoApellido,
                        fecha_nacimiento   = fechaNacimiento,
                        correo_electronico = correoElectronico,
                        contrasenia        = contrasenia
                    )
                    val resultado = repositorio.registrarUsuario(usuario)

                    if (resultado.isSuccess) {
                        enviarCorreoBienvenida(correoElectronico)
                        _estadoUi.value = _estadoUi.value.copy(registroUsuarioExitoso = true)
                    } else {
                        val msg = resultado.exceptionOrNull()?.message ?: "Error desconocido"
                        _estadoUi.value = _estadoUi.value.copy(mensajeError = msg)
                    }
                }
            }
        }
    }

    suspend fun enviarCorreoBienvenida(email: String) {
        try {

            val response = N8nClient.api.enviarCorreoBienvenida(
                BienvenidaRequest(email)
            )

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
        _estadoUi.value = _estadoUi.value.copy(mensajeError = "")
    }
    fun limpiarEstadoRegistro() {
        _estadoUi.value = _estadoUi.value.copy(registroUsuarioExitoso = false)
    }
}