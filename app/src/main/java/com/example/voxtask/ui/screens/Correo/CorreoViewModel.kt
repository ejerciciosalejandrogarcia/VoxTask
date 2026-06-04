package com.example.voxtask.ui.screens.Correo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Correo
import com.example.voxtask.databases.network.ClienteN8n
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.voxtask.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
/**
 * Define los posibles estados de la interfaz en la pantalla
 */
sealed class CorreoUiState {
    object Cargando : CorreoUiState()
    object NecesitaConectarGoogle : CorreoUiState()
    data class Exito(val correos: List<Correo>) : CorreoUiState()
    data class Error(val mensaje: Int, val esErrorDeCarga: Boolean = false) : CorreoUiState()
}

class CorreoViewModel : ViewModel() {
    /** Variables */
    private val autenticacion = FirebaseAuth.getInstance()
    private val baseDatos = FirebaseFirestore.getInstance()
    private val _estadoUi = MutableStateFlow<CorreoUiState>(CorreoUiState.Cargando)
    val uiState: StateFlow<CorreoUiState> = _estadoUi
    private val _canalError = Channel<String>(Channel.BUFFERED)
    val errorFlow = _canalError.receiveAsFlow()

    /**
     * Permite validar la cuenta de Google,
     * obtiene el token de acceso, lo almacena en Firestore
     * y dispara la carga de los correos electrónicos.
     */
    fun iniciar(contexto: Context) {
        viewModelScope.launch {
            _estadoUi.value = CorreoUiState.Cargando

            val uid = autenticacion.currentUser?.uid
            android.util.Log.d("CORREO_DEBUG", "uid: $uid")

            if (uid == null) {
                _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_sin_sesion))
                return@launch
            }

            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                val emailFirebase = autenticacion.currentUser?.email ?: ""
                android.util.Log.d("CORREO_DEBUG", "cuentaGoogle: ${cuentaGoogle?.email}, emailFirebase: $emailFirebase, account: ${cuentaGoogle?.account}")

                if (cuentaGoogle?.account != null && cuentaGoogle.email == emailFirebase) {
                    android.util.Log.d("CORREO_DEBUG", "Entrando a obtener token")

                    val tokenAcceso = try {
                        withContext(Dispatchers.IO) {
                            val tokenActual = GoogleAuthUtil.getToken(
                                contexto,
                                cuentaGoogle.account!!,
                                "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                            )
                            GoogleAuthUtil.clearToken(contexto, tokenActual)
                            GoogleAuthUtil.getToken(
                                contexto,
                                cuentaGoogle.account!!,
                                "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("CORREO_DEBUG", "Error obteniendo token: ${e.message}")
                        _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_general))
                        return@launch
                    }

                    android.util.Log.d("CORREO_DEBUG", "Token obtenido OK, llamando cargarCorreos")

                    baseDatos.collection("usuarios")
                        .document(uid)
                        .set(
                            mapOf("gmailAccessToken" to tokenAcceso),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                        .await()

                    cargarCorreos(contexto, tokenAcceso)
                    android.util.Log.d("CORREO_DEBUG", "cargarCorreos terminó, uiState: ${_estadoUi.value}")

                } else {
                    android.util.Log.d("CORREO_DEBUG", "NecesitaConectarGoogle")
                    _estadoUi.value = CorreoUiState.NecesitaConectarGoogle
                }

            } catch (e: Exception) {
                android.util.Log.d("CORREO_DEBUG", "Exception general: ${e.message}")
                _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_general))
            }
        }
    }

    /**
     * Permite solicitar la lista de correos utilizando el token de autenticación
     */
    private suspend fun cargarCorreos(contexto: Context, token: String) {
        try {
            android.util.Log.d("CORREO_DEBUG", "Llamando N8nClient...")
            val correos = ClienteN8n.api.obtenerCorreos(token)
            android.util.Log.d("CORREO_DEBUG", "Correos recibidos: ${correos.size}")
            _estadoUi.value = CorreoUiState.Exito(correos)
        } catch (e: Exception) {
            android.util.Log.d("CORREO_DEBUG", "Error cargando correos: ${e.message}")
            _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_cargar_correos))
            _estadoUi.value = CorreoUiState.Error(
                mensaje = R.string.txt_error+R.string.error_cargar_correos,
                esErrorDeCarga = true
            )
        }
    }

    /**
     * Obtiene el token de acceso de Google, lo guarda en Firestore y dispara la carga inicial de los correos.
     */
    fun guardarTokenYCargarCorreos(contexto: Context, codigoAutorizacion: String?) {
        viewModelScope.launch {
            _estadoUi.value = CorreoUiState.Cargando
            val uid = autenticacion.currentUser?.uid ?: return@launch
            val emailFirebase = autenticacion.currentUser?.email ?: ""

            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                    ?: throw Exception()

                if (cuentaGoogle.email != emailFirebase) {
                    _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_cuenta_google_incorrecta))
                    return@launch
                }

                val tokenAcceso = withContext(Dispatchers.IO) {
                    val tokenActual = GoogleAuthUtil.getToken(
                        contexto,
                        cuentaGoogle.account!!,
                        "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                    )
                    GoogleAuthUtil.clearToken(contexto, tokenActual)
                    GoogleAuthUtil.getToken(
                        contexto,
                        cuentaGoogle.account!!,
                        "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                    )
                }

                baseDatos.collection("usuarios")
                    .document(uid)
                    .set(
                        mapOf("gmailAccessToken" to tokenAcceso),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .await()

                cargarCorreos(contexto, tokenAcceso)

            } catch (e: Exception) {
                android.util.Log.d("CORREO_DEBUG", "guardarToken Exception: ${e.message}")
                _canalError.send(contexto.getString(R.string.txt_error)+contexto.getString(R.string.error_general))
            }
        }
    }

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