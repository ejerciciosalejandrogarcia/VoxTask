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
import com.example.voxtask.ui.theme.ThemeManager
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.google.firebase.FirebaseApp
import android.Manifest
import android.content.Context
import android.content.res.Configuration
import com.example.voxtask.utils.TextoAVoz

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    var navController: NavController? = null
    private val plantillaBaseViewModel: PlantillaBaseViewModel by viewModels()
    private lateinit var themeManager: ThemeManager

    // 👇 Guardamos el intent pendiente hasta que el NavController esté listo
    private var intentPendiente: Intent? = null

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = prefs.getString("idioma", "es") ?: "es"
        val locale = java.util.Locale(idioma)
        TextoAVoz.localeActual = locale
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
        themeManager = ThemeManager(applicationContext)
        pedirPermiso.launch(Manifest.permission.RECORD_AUDIO)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // 👇 Guardar el intent solo en la primera creación real
        if (savedInstanceState == null) {
            intentPendiente = intent
        }

        setContent {
            VoxTaskTheme(themeManager = themeManager) {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    val windowSize = calculateWindowSizeClass(this)
                    VoxTaskApp(
                        windowSize             = windowSize.widthSizeClass,
                        plantillaBaseViewModel = plantillaBaseViewModel,
                        onNavControllerReady   = { nc ->
                            navController = nc
                            // 👇 Ahora sí tenemos NavController: procesar el intent pendiente
                            intentPendiente?.let {
                                manejarIntent(it)
                                intentPendiente = null  // Limpiar para no reprocesar
                            }
                        },
                        deepLinkIntent = intent
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Si el NavController ya está listo, procesar ahora
        // Si no, guardarlo como pendiente
        if (navController != null) {
            manejarIntent(intent)
        } else {
            intentPendiente = intent
        }
    }

    private fun manejarIntent(intent: Intent?) {
        if (intent?.action == "ABRIR_CONTADOR") {
            navController?.navigate(VoxTaskScreen.Contador.name) {
                launchSingleTop = true
            }
            setIntent(Intent())
            return
        }

        val data = intent?.data
        if (data?.scheme == "voxtask" && data.host == "nuevacontrasena") {
            val oobCode = data.getQueryParameter("oobCode") ?: ""
            navController?.navigate("${VoxTaskScreen.RegistrarNuevaContrasenia.name}?oobCode=$oobCode")
            setIntent(Intent())
        }
    }
}