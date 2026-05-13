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
data class InicioSesionUiState(
    val nombreUsuario: String = "",
    val contrasena: String = "",
    val inicioSesionExitoso: Boolean = false,
    val mensajeError: String = ""
)

class InicioSesionViewModel(
    private val usuarioRepository: UsuarioDao = UsuarioRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _estadoUi = MutableStateFlow(InicioSesionUiState())
    val estadoUi: StateFlow<InicioSesionUiState> = _estadoUi.asStateFlow()

    fun alCambiarNombreUsuario(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(nombreUsuario = valor)
    }

    fun alCambiarContrasena(valor: String) {
        _estadoUi.value = _estadoUi.value.copy(contrasena = valor)
    }

    fun iniciarSesion() {
        val nombreUsuario = _estadoUi.value.nombreUsuario.trim()
        val contrasena = _estadoUi.value.contrasena.trim()

        val regexContrasenia = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$")
        val regexNombreUsuario = Regex("^[a-zA-Z0-9]+$")

        when {
            nombreUsuario.isBlank() || contrasena.isBlank() -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "CAMPOS_VACIOS")
                return
            }
            !regexNombreUsuario.matches(nombreUsuario) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "USUARIO_INVALIDO")
                return
            }
            !regexContrasenia.matches(contrasena) -> {
                _estadoUi.value = _estadoUi.value.copy(mensajeError = "CONTRASENIA_DEBIL")
                return
            }
            else -> {
                viewModelScope.launch {
                    val resultado = usuarioRepository.iniciarSesion(nombreUsuario, contrasena)
                    resultado.onSuccess {
                        _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = true, mensajeError = "")
                    }.onFailure {
                        _estadoUi.value = _estadoUi.value.copy(mensajeError = it.message ?: "ERROR_CREDENTIALS")
                    }
                }
            }
        }
    }

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
                    _estadoUi.value = _estadoUi.value.copy(mensajeError = "ERROR_GOOGLE")
                }
            }
    }

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
                    .addOnFailureListener { _estadoUi.value = _estadoUi.value.copy(mensajeError = "ERROR_FIRESTORE") }
            }
        }.addOnFailureListener {
            _estadoUi.value = _estadoUi.value.copy(mensajeError = "ERROR_CONEXION")
        }
    }

    fun limpiarError() { _estadoUi.value = _estadoUi.value.copy(mensajeError = "") }
    fun limpiarEstadoInicioSesion() { _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = false) }

    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // He puesto el client_id de tipo 3 de tu JSON
            .requestIdToken("820155883821-7trt2n6ghi9hlk6m039rl376reh5vjsj.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(contexto, gso)
    }
}