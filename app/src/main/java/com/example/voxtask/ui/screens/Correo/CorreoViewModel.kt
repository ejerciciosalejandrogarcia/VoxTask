package com.example.voxtask.ui.screens.Correo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.databases.model.Correo
import com.example.voxtask.databases.network.N8nClient
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class CorreoUiState {
    object Cargando : CorreoUiState()
    object NecesitaConectarGoogle : CorreoUiState()
    data class Exito(val correos: List<Correo>) : CorreoUiState()
    data class Error(val mensaje: String) : CorreoUiState()
}

class CorreoViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CorreoUiState>(CorreoUiState.Cargando)
    val uiState: StateFlow<CorreoUiState> = _uiState

    fun iniciar(contexto: Context) {
        viewModelScope.launch {
            _uiState.value = CorreoUiState.Cargando
            val uid = auth.currentUser?.uid ?: run {
                _uiState.value = CorreoUiState.Error("No hay sesión activa")
                return@launch
            }

            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                val emailFirebase = auth.currentUser?.email ?: ""

                if (cuentaGoogle?.account != null && cuentaGoogle.email == emailFirebase) {
                    val accessToken = withContext(Dispatchers.IO) {
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

                    firestore.collection("usuarios")
                        .document(uid)
                        .set(
                            mapOf("gmailAccessToken" to accessToken),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                        .await()

                    cargarCorreos(accessToken)
                } else {
                    _uiState.value = CorreoUiState.NecesitaConectarGoogle
                }

            } catch (e: Exception) {
                _uiState.value = CorreoUiState.Error("Error: ${e.message}")
            }
        }
    }

    private suspend fun cargarCorreos(token: String) {
        try {
            val correos = N8nClient.api.obtenerCorreos(token)
            _uiState.value = CorreoUiState.Exito(correos)
        } catch (e: Exception) {
            _uiState.value = CorreoUiState.Error("Error: ${e.javaClass.simpleName} - ${e.message}")
        }
    }

    fun obtenerClienteGoogle(contexto: Context): GoogleSignInClient {
        val emailFirebase = FirebaseAuth.getInstance().currentUser?.email ?: ""

        val cliente = GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
                .build()
        )
        cliente.signOut()

        return GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
                .setAccountName(emailFirebase)
                .build()
        )
    }

    fun guardarTokenYCargarCorreos(contexto: Context, serverAuthCode: String?) {
        viewModelScope.launch {
            _uiState.value = CorreoUiState.Cargando
            val uid = auth.currentUser?.uid ?: return@launch
            val emailFirebase = auth.currentUser?.email ?: ""

            try {
                val cuentaGoogle = GoogleSignIn.getLastSignedInAccount(contexto)
                    ?: throw Exception("No hay cuenta Google")

                if (cuentaGoogle.email != emailFirebase) {
                    _uiState.value = CorreoUiState.Error(
                        "La cuenta Google debe ser $emailFirebase"
                    )
                    return@launch
                }

                val accessToken = withContext(Dispatchers.IO) {
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

                firestore.collection("usuarios")
                    .document(uid)
                    .set(
                        mapOf("gmailAccessToken" to accessToken),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .await()

                cargarCorreos(accessToken)

            } catch (e: Exception) {
                _uiState.value = CorreoUiState.Error("Error: ${e.message}")
            }
        }
    }
}