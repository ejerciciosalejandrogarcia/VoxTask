package com.example.voxtask

import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.voxtask.ui.theme.VoxTaskTheme
import androidx.compose.material3.Surface
import com.google.firebase.FirebaseApp
import android.Manifest
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    //Pedir permiso para el microfono a la hora de que arranque la app
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
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val windowSize = calculateWindowSizeClass(this)
                    VoxTaskApp(
                        windowSize = windowSize.widthSizeClass
                    )
                }
            }
        }
    }
}

