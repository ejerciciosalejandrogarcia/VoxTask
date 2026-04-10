package com.example.voxtask.ui.screens.Inicio_Sesion

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.voxtask.R
import com.example.voxtask.databases.dao.UsuarioDao
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.databases.repository.UsuarioRepository
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InicioSesionUiState(
    val nombreUsuario: String = "",
    val contrasena: String = "",
    val inicioSesionExitoso: Boolean = false,
    val mensajeError: String = "",
    val gmailAuthCode: String = ""
)

class InicioSesionViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioDao()  // ← añadir
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

        if (nombreUsuario.isBlank() || contrasena.isBlank()) {
            _estadoUi.value = _estadoUi.value.copy(
                mensajeError = "Rellena todos los campos"
            )
            return
        }

        viewModelScope.launch {
            val resultado = usuarioRepository.iniciarSesion(nombreUsuario, contrasena)

            resultado.onSuccess {
                _estadoUi.value = _estadoUi.value.copy(
                    inicioSesionExitoso = true,
                    mensajeError = ""
                )
            }.onFailure {
                _estadoUi.value = _estadoUi.value.copy(
                    mensajeError = it.message ?: "Credenciales incorrectas"
                )
            }
        }
    }
    // Login con cuenta Google

    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(contexto.getString(R.string.default_web_client_id))
            .requestServerAuthCode(contexto.getString(R.string.default_web_client_id)) // ← AÑADIR
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly")) // ← AÑADIR
            .build()
        return GoogleSignIn.getClient(contexto, opciones)
    }
    fun autenticarConGoogle(tokenGoogle: String, serverAuthCode: String? = null) {
        val credencial = GoogleAuthProvider.getCredential(tokenGoogle, null)

        auth.signInWithCredential(credencial)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val usuarioFirebase = auth.currentUser
                    if (usuarioFirebase == null) return@addOnCompleteListener

                    if (serverAuthCode != null) {
                        firestore.collection("usuarios")
                            .document(usuarioFirebase.uid)
                            .update("gmailAuthCode", serverAuthCode)
                            .addOnSuccessListener {
                                Log.d("Firestore", "✅ gmailAuthCode guardado")
                            }
                            .addOnFailureListener {
                                Log.e("Firestore", "❌ Error guardando gmailAuthCode: ${it.message}")
                            }
                    }

                    guardarOActualizarUsuarioEnFirestore(usuarioFirebase.uid) {
                        Log.d("Firestore", "✅ Navegando a home")
                        _estadoUi.value = _estadoUi.value.copy(inicioSesionExitoso = true)
                    }
                } else {
                    _estadoUi.value = _estadoUi.value.copy(
                        mensajeError = "Error al iniciar sesión con Google"
                    )
                }
            }
    }

    private fun guardarOActualizarUsuarioEnFirestore(uid: String, alTerminar: () -> Unit) {
        val usuarioFirebase = auth.currentUser
        Log.d("Firestore", "Intentando guardar usuario con UID: $uid")

        if (usuarioFirebase == null) {
            Log.e("Firestore", "❌ currentUser es null en guardar")
            return
        }

        val ref = firestore.collection("usuarios").document(uid)
        Log.d("Firestore", "Referencia creada: ${ref.path}")

        ref.get()
            .addOnSuccessListener { documento ->
                Log.d("Firestore", "Documento obtenido. Existe: ${documento.exists()}")
                if (documento.exists()) {
                    Log.d("Firestore", "✅ Usuario ya existe, navegando")
                    alTerminar()
                } else {
                    Log.d("Firestore", "Usuario nuevo, creando documento...")
                    val nombreCompleto = usuarioFirebase.displayName ?: ""
                    val partes = nombreCompleto.trim().split(" ")

                    val nuevoUsuario = Usuario(
                        id = uid,
                        nombre_usuario = usuarioFirebase.email?.substringBefore("@") ?: uid,
                        nombre = partes.getOrElse(0) { "" },
                        primer_apellido = partes.getOrElse(1) { "" },
                        segundo_apellido = partes.drop(2).joinToString(" "),
                        fecha_nacimiento = "",
                        correo_electronico = usuarioFirebase.email ?: "",
                        contrasenia = "",
                        verificado = usuarioFirebase.isEmailVerified,
                        fecha_creacion = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                        ).format(Date())
                    )

                    Log.d("Firestore", "Usuario a guardar: $nuevoUsuario")

                    ref.set(nuevoUsuario)
                        .addOnSuccessListener {
                            Log.d("Firestore", "✅ Usuario guardado correctamente en Firestore")
                            alTerminar()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "❌ Error al guardar: ${e.message}")
                            _estadoUi.value = _estadoUi.value.copy(
                                mensajeError = "Error al guardar datos: ${e.message}"
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Error al obtener documento: ${e.message}")
                _estadoUi.value = _estadoUi.value.copy(
                    mensajeError = "Error al conectar con Firestore: ${e.message}"
                )
            }
    }}