package com.example.voxtask.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.voxtask.ui.theme.ThemeManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.voxtask.ui.screens.Ajustes.AjustesViewModel
import java.util.Locale

class PlantillaBaseViewModel : ViewModel() {

    //Variables
    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()
    // Estado para la URI del fondo personalizado
    var fondoPersonalizadoUri by mutableStateOf<Uri?>(null)
    private val _fondoUri = MutableStateFlow<Uri?>(null)
    val fondoUri: StateFlow<Uri?> = _fondoUri.asStateFlow()

    fun actualizarFondo(uri: Uri?) {
        _fondoUri.value = uri
        fondoPersonalizadoUri = uri
    }

    //Funcion que cierra sesion del usuario logueado y cierra la aplicacion
    fun cerrarSesion(
        contexto: Context,
        actividad: Activity,
        alCerrar: () -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            firestore.collection("usuarios")
                .document(uid)
                .update("gmailAccessToken", "")
        }

        val clienteGoogle = GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )

        clienteGoogle.signOut().addOnCompleteListener {
            auth.signOut()
            resetearIdiomaEspanol(contexto)  // ← directo, sin jaleo
            val themeManager = ThemeManager(contexto)
            themeManager.resetearColores()
            alCerrar()
            actividad.finishAffinity()
        }
    }
    fun resetearIdiomaEspanol(contexto: Context) {
        val locale = Locale("es")
        Locale.setDefault(locale)

        val config = Configuration(contexto.resources.configuration)
        config.setLocale(locale)
        contexto.resources.updateConfiguration(config, contexto.resources.displayMetrics)

        contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .edit()
            .putString("idioma", "es")
            .commit()
    }
}