package com.example.voxtask

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voxtask.ui.screens.Inicio.InicioScreen
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionScreen
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionViewModel
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioScreen
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

enum class VoxTaskScreen {
    Inicio_sesion,
    Registro_Usuario,
    Inicio
}

@Composable
fun VoxTaskApp(
    windowSize: WindowWidthSizeClass,
) {
    val navController = rememberNavController()
    val contexto = LocalContext.current
    val viewModel: InicioSesionViewModel = viewModel()
    val viewModelRegistrar: RegistroUsuarioViewModel = viewModel()

    // Lanzador que recibe el resultado de la pantalla de Google
    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        Log.d("Google", "ResultCode: ${resultado.resultCode}")
        Log.d("Google", "Data: ${resultado.data}")

        if (resultado.resultCode == Activity.RESULT_OK) {
            val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
            try {
                val cuenta = tarea.getResult(ApiException::class.java)
                Log.d("Google", "Token obtenido: ${cuenta.idToken}")
                viewModel.autenticarConGoogle(cuenta.idToken!!)
            } catch (e: ApiException) {
                Log.e("Google", "ApiException: ${e.statusCode} - ${e.message}")
            }
        } else {
            Log.e("Google", "❌ El usuario canceló o hubo un error")
        }
    }

    NavHost(
        navController = navController,
        startDestination = VoxTaskScreen.Inicio_sesion.name
    ) {
        composable(route = VoxTaskScreen.Inicio_sesion.name) {
            InicioSesionScreen(
                alIniciarSesionExitosamente = {
                    navController.navigate(VoxTaskScreen.Inicio.name){
                        // Esto evita que el usuario pueda volver atrás al login
                        popUpTo(VoxTaskScreen.Inicio_sesion.name) { inclusive = true }

                    }
                },
                alNavegarARegistro = {
                    navController.navigate(VoxTaskScreen.Registro_Usuario.name)
                },
                alPulsarGoogle = {
                    // Aquí lanzamos la pantalla de Google
                    val clienteGoogle = viewModel.obtenerClienteGoogle(contexto)
                    lanzadorGoogle.launch(clienteGoogle.signInIntent)
                },
                modeloVista = viewModel
            )
        }

        composable(VoxTaskScreen.Registro_Usuario.name) {
            RegistroUsuarioScreen(
                alRegistroExitoso = {             navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                    popUpTo(VoxTaskScreen.Registro_Usuario.name) { inclusive = true }
                }
                },
                modeloVista = viewModelRegistrar
            )
        }

        composable(VoxTaskScreen.Inicio.name) {
            InicioScreen(

            )
        }
    }
}

