package com.example.voxtask.ui.screens.Inicio_Sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.voxtask.databases.repository.UsuarioRepository
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.databases.dao.UsuarioDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Context
import com.example.voxtask.R
import com.google.android.gms.common.api.Scope
/**
 * Representa el estado de la UI
 */
data class InicioSesionUiState(
    val nombreUsuario: String = "",
    val contrasena: String = "",
    val inicioSesionExitoso: Boolean = false,
    val mensajeError: Int? = null
)

class InicioSesionViewModel(
    private val usuarioRepository: UsuarioDao = UsuarioRepository()
) : ViewModel() {

    /** Variables */
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _estadoUi = MutableStateFlow(InicioSesionUiState())
    val estadoUi: StateFlow<InicioSesionUiState> = _estadoUi.asStateFlow()

    /** Actualiza el nombre de usuario en el estado */
    fun alCambiarNombreUsuario(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombreUsuario = valor)
    }
    /** Actualiza la contraseña en el estado */
    fun alCambiarContrasena(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(contrasena = valor)
    }
    /** Permite gestionar los posibles errores a la hora de iniciar sesion y si es exitoso el usuario se le redijira a 'Verificacion' */
    fun iniciarSesion() {
        val nombreUsuario = _estadoUi.value.nombreUsuario.trim()
        val contrasena = _estadoUi.value.contrasena.trim()

        val regexContrasenia = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$")
        val regexNombreUsuario = Regex("^[a-zA-Z0-9]+$")

        when {
            nombreUsuario.isBlank() || contrasena.isBlank() -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_campos_vacios)
                return
            }
            !regexNombreUsuario.matches(nombreUsuario) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_nombre_usuario_invalido)
                return
            }
            !regexContrasenia.matches(contrasena) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_contrasenia_debil)
                return
            }
            else -> {
                viewModelScope.launch {
                    val resultado = usuarioRepository.iniciarSesion(nombreUsuario, contrasena)
                    resultado.onSuccess {
                        _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = true, mensajeError = null)
                    }.onFailure {
                        _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_credenciales_incorrectas)
                    }
                }
            }
        }
    }

    /**
     * Permite autenticar al usuario en Firebase utilizando las credenciales de Google y actualiza
     * el usuario, marcando el estado de inicio de sesión como exitoso
     */
    fun autenticarConGoogle(tokenGoogle: String, serverAuthCode: String? = null) {
        val credencial = GoogleAuthProvider.getCredential(tokenGoogle, null)

        auth.signInWithCredential(credencial)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val usuarioFirebase = auth.currentUser ?: return@addOnCompleteListener

                    if (serverAuthCode != null) {
                        firestore.collection("usuarios")
                            .document(usuarioFirebase.uid)
                            .update("gmailAuthCode", serverAuthCode)
                    }

                    guardarOActualizarUsuarioEnFirestore(usuarioFirebase.uid) {
                        _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = true)
                    }
                } else {
                    _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.txt_error+R.string.err_google_auth)
                }
            }
    }
    /**
     * Permiet inicializar o actualizar el perfil del usuario en Firestore si no existe
     */
    private fun guardarOActualizarUsuarioEnFirestore(uid: String, alTerminar: () -> Unit) {
        val usuarioFirebase = auth.currentUser ?: return
        val ref = firestore.collection("usuarios").document(uid)

        ref.get().addOnSuccessListener { documento ->
            if (documento.exists()) {
                alTerminar()
            } else {
                val nombreCompleto = usuarioFirebase.displayName ?: ""
                val partes = nombreCompleto.trim().split(" ")
                val nuevoUsuario = Usuario(
                    id = uid,
                    nombre_usuario = usuarioFirebase.email?.substringBefore("@") ?: uid,
                    nombre = partes.getOrElse(0) { "" },
                    primer_apellido = partes.getOrElse(1) { "" },
                    segundo_apellido = partes.drop(2).joinToString(" "),
                    fecha_creacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    correo_electronico = usuarioFirebase.email ?: ""
                )

                ref.set(nuevoUsuario)
                    .addOnSuccessListener { alTerminar() }
                    .addOnFailureListener { _estadoUi.value = _estadoUi.value.copy(mensajeError =  R.string.txt_error+R.string.err_firestore) }
            }
        }.addOnFailureListener {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = R.string.err_conexion)
        }
    }

    /**
     * Permite limpiar los mensajes de error de la pantalla 'Cambiar Contrasenia'
     */
    fun limpiarError() { _estadoUi.value = _estadoUi.value.copy(mensajeError = null) }
    /**
     * Permite reiniciar la UI
     */
    fun limpiarEstadoInicioSesion() { _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = false) }

    /**
     * Permite devolver el cliente de inicio de sesión de Google con los permisos
     * necesarios para acceder a la API de Gmail en modo lectura
     */
    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("820155883821-7trt2n6ghi9hlk6m039rl376reh5vjsj.apps.googleusercontent.com")
            .requestServerAuthCode("820155883821-7trt2n6ghi9hlk6m039rl376reh5vjsj.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .build()
        return GoogleSignIn.getClient(contexto, opciones)
    }
}

