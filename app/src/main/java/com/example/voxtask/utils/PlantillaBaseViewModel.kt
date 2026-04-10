package com.example.voxtask.utils

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlantillaBaseViewModel : ViewModel() {

    //Variables
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    //Funcion que cierra sesion del usuario logueado y cierra la aplicacion
    fun cerrarSesion(contexto: Context, actividad: Activity, alCerrar: () -> Unit) {
        val uid = auth.currentUser?.uid

        // Borrar el gmailAccessToken de Firestore
        if (uid != null) {
            firestore.collection("usuarios")
                .document(uid)
                .update("gmailAccessToken", "")
        }

        // Cerrar sesión de Google primero, luego Firebase
        val clienteGoogle = GoogleSignIn.getClient(
            contexto,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )

        clienteGoogle.signOut().addOnCompleteListener {
            auth.signOut()
            alCerrar()
            actividad.finishAffinity()
        }
    }
}