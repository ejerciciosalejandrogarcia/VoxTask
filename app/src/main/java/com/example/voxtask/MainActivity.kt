package com.example.voxtask

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.NavController
import com.example.voxtask.ui.theme.VoxTaskTheme
import com.google.firebase.FirebaseApp
import android.Manifest
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    var navController: NavController? = null

    //Asegura que se use el idioma elegido en ajustes cada vez que se abra la aplicacion
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = prefs.getString("idioma", "es") ?: "es"

        val locale = java.util.Locale(idioma)
        java.util.Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
    //Funcion para pedir permiso al usuario para acceder al microfoo del dispositivo
    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            android.util.Log.e("VOZ", "Permiso denegado por el usuario")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pedirPermiso.launch(Manifest.permission.RECORD_AUDIO)

        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            VoxTaskTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    val windowSize = calculateWindowSizeClass(this)
                    VoxTaskApp(
                        windowSize = windowSize.widthSizeClass,
                        onNavControllerReady = { navController = it }
                    )
                }
            }
        }
    }

    // Cuando el usuario toca la notificación
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == "ABRIR_CONTADOR") {
            navController?.navigate(VoxTaskScreen.Contador.name)
        }
    }


}