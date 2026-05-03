package com.example.voxtask

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.NavController
import com.example.voxtask.ui.theme.VoxTaskTheme
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.google.firebase.FirebaseApp
import android.Manifest
import android.content.Context
import android.content.res.Configuration
import com.example.voxtask.ui.theme.rememberThemeManager

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    var navController: NavController? = null

    // ✅ Una sola instancia compartida por toda la app
    private val plantillaBaseViewModel: PlantillaBaseViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = prefs.getString("idioma", "es") ?: "es"
        val locale = java.util.Locale(idioma)
        java.util.Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

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
            val themeManager = rememberThemeManager()
            VoxTaskTheme(themeManager = themeManager) {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    val windowSize = calculateWindowSizeClass(this)

                    // Captura el deep link si viene de un email
                    val deepLinkIntent = intent

                    VoxTaskApp(
                        windowSize = windowSize.widthSizeClass,
                        plantillaBaseViewModel = plantillaBaseViewModel,
                        onNavControllerReady = { navController = it },
                        deepLinkIntent = deepLinkIntent  // ← añade esto
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)  // ← importante para capturar el intent nuevo
        if (intent.action == "ABRIR_CONTADOR") {
            navController?.navigate(VoxTaskScreen.Contador.name)
        }
        // Deep link de nueva contraseña
        val data = intent.data
        if (data?.scheme == "voxtask" && data.host == "nuevacontrasena") {
            val oobCode = data.getQueryParameter("oobCode") ?: ""
            navController?.navigate("${VoxTaskScreen.RegistrarNuevaContrasenia.name}?oobCode=$oobCode")
        }
    }
}