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
    /** Variables */
    var navController: NavController? = null
    private val plantillaBaseViewModel: PlantillaBaseViewModel by viewModels()
    private lateinit var gestorTema: ThemeManager
    private var intencionPendiente: Intent? = null

    /**
     * Permite asegurar que el idioma seleccionado por el usuario se aplique desde el inicio
     * */
    override fun attachBaseContext(nuevaBase: Context) {
        val preferencias = nuevaBase.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idioma = preferencias.getString("idioma", "es") ?: "es"
        val configuracionRegional = java.util.Locale(idioma)

        TextoAVoz.localeActual = configuracionRegional
        val configuracion = Configuration()
        configuracion.setLocale(configuracionRegional)
        super.attachBaseContext(nuevaBase.createConfigurationContext(configuracion))
    }

    /**
     * Registra la respuesta del usuario ante la solicitud de permisos
     * del micrófono, manejando el caso en que el acceso sea denegado
     */
    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            android.util.Log.e("VOZ", "Permiso denegado por el usuario")
        }
    }

    /**
     * Inicializa los servicios básicos, solicita permisos de audio,
     * configura la navegación y renderiza la interfaz principal
     */
    override fun onCreate(estadoGuardado: Bundle?) {
        super.onCreate(estadoGuardado)
        gestorTema = ThemeManager(applicationContext)
        pedirPermiso.launch(Manifest.permission.RECORD_AUDIO)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        if (estadoGuardado == null) {
            intencionPendiente = intent
        }

        setContent {
            VoxTaskTheme(themeManager = gestorTema) {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    val tamanioPantalla = calculateWindowSizeClass(this)
                    VoxTaskApp(
                        windowSize             = tamanioPantalla.widthSizeClass,
                        plantillaBaseViewModel = plantillaBaseViewModel,
                        onNavControllerReady   = { nc ->
                            navController = nc
                            intencionPendiente?.let {
                                manejarIntent(it)
                                intencionPendiente = null
                            }
                        },
                        deepLinkIntent = intent
                    )
                }
            }
        }
    }
    /**
     * Permite garantizar que la aplicación no pierda ni ignore ninguna instrucción externa,
     * como enlaces o notificaciones
     */
    override fun onNewIntent(intencion: Intent) {
        super.onNewIntent(intencion)
        setIntent(intencion)
        if (navController != null) {
            manejarIntent(intencion)
        } else {
            intencionPendiente = intencion
        }
    }

    /**
     * Permite analizar y procesar los comandos de navegación recibidos,
     * redirigiendo al usuario a la pantalla correspondiente según la acción o enlace
     */
    private fun manejarIntent(intencion: Intent?) {
        if (intencion?.action == "ABRIR_CONTADOR") {
            navController?.navigate(VoxTaskScreen.Contador.name) {
                launchSingleTop = true
            }
            setIntent(Intent())
            return
        }

        val datos = intencion?.data
        if (datos?.scheme == "voxtask" && datos.host == "nuevacontrasena") {
            val codigoOob = datos.getQueryParameter("oobCode") ?: ""
            navController?.navigate("${VoxTaskScreen.RegistrarNuevaContrasenia.name}?oobCode=$codigoOob")
            setIntent(Intent())
        }
    }
}