package com.example.voxtask

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voxtask.ui.screens.Ajustes.AjustesScreen
import com.example.voxtask.ui.screens.Ajustes.AjustesViewModel
import com.example.voxtask.ui.screens.Contador.ContadorScreen
import com.example.voxtask.ui.screens.Contador.ContadorViewModel
import com.example.voxtask.ui.screens.Correo.CorreoScreen
import com.example.voxtask.ui.screens.Correo.CorreoViewModel
import com.example.voxtask.ui.screens.Lista_Compra.ListaCompraScreen
import com.example.voxtask.ui.screens.Lista_Compra.ListaCompraViewModel
import com.example.voxtask.ui.screens.Inicio.InicioScreen
import com.example.voxtask.ui.screens.Inicio.InicioViewModel
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionScreen
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionViewModel
import com.example.voxtask.ui.screens.Perfil.PerfilScreen
import com.example.voxtask.ui.screens.Perfil.PerfilViewModel
import com.example.voxtask.ui.screens.Recordatorio.RecordatorioScreen
import com.example.voxtask.ui.screens.Recordatorio.RecordatorioViewModel
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioScreen
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioViewModel
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

enum class VoxTaskScreen {
    Inicio_sesion,
    Registro_Usuario,
    Inicio,
    Contador,
    ListaCompra,
    Recordatorio,
    Correo,
    Ajustes,
    Perfil
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VoxTaskApp(
    windowSize: WindowWidthSizeClass,
    onNavControllerReady: (NavController) -> Unit = {} // ← añade esto

) {
    val navController = rememberNavController()
    val contexto = LocalContext.current

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    val viewModelInicioSesion: InicioSesionViewModel = viewModel()
    val viewModelRegistrar: RegistroUsuarioViewModel = viewModel()
    val viewModelInicio: InicioViewModel = viewModel()

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
                Log.d("Google", "ServerAuthCode: ${cuenta.serverAuthCode}")
                viewModelInicioSesion.autenticarConGoogle(
                    tokenGoogle = cuenta.idToken!!,
                    serverAuthCode = cuenta.serverAuthCode
                )
            } catch (e: ApiException) {
                Log.e("Google", "ApiException: ${e.statusCode} - ${e.message}")
            }
        } else {
            Log.e("Google", "El usuario canceló o hubo un error")
        }
    }

    NavHost(
        navController = navController,
        startDestination = VoxTaskScreen.Inicio_sesion.name
    ) {

        //Screen Inicio Sesion
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
                    // Pantalla de Google
                    val clienteGoogle = viewModelInicioSesion.obtenerClienteGoogle(contexto)
                    lanzadorGoogle.launch(clienteGoogle.signInIntent)
                },
                viewModel = viewModelInicioSesion
            )
        }

        //Screen Registro
        composable(VoxTaskScreen.Registro_Usuario.name) {
            RegistroUsuarioScreen(
                alRegistroExitoso = {             navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                    popUpTo(VoxTaskScreen.Registro_Usuario.name) { inclusive = true }
                }
                },
                viewModel = viewModelRegistrar
            )
        }


        //Screen Inicio
        composable(VoxTaskScreen.Inicio.name) {
            viewModelInicio.abrirContador = {
                navController.navigate(VoxTaskScreen.Contador.name) {
                    popUpTo(VoxTaskScreen.Inicio.name)
                }
            }
            viewModelInicio.abrirListaCompra = {
                navController.navigate(VoxTaskScreen.ListaCompra.name) {
                    popUpTo(VoxTaskScreen.Inicio.name)
                }
            }
            viewModelInicio.abrirRecordatorio = {
                navController.navigate(VoxTaskScreen.Recordatorio.name) {
                    popUpTo(VoxTaskScreen.Inicio.name)
                }
            }
            viewModelInicio.abrirCorreo = {
                navController.navigate(VoxTaskScreen.Correo.name) {
                    popUpTo(VoxTaskScreen.Inicio.name)
                }
            }
            InicioScreen(
                viewModel = viewModelInicio,
                navController = navController
            )
        }

        //Screen Contador
        composable(VoxTaskScreen.Contador.name) {
            val viewModelContador: ContadorViewModel = viewModel()
            ContadorScreen(viewModel = viewModelContador,navController)
        }


        //Screen Lista de la Compra
        composable(VoxTaskScreen.ListaCompra.name) {
            val viewModelListaCompra: ListaCompraViewModel = viewModel()
            val viewModelPlantilla: PlantillaBaseViewModel = viewModel()

            ListaCompraScreen(
                viewModelPlantilla = viewModelPlantilla,
                viewModel = viewModelListaCompra,
                navController = navController
            )
        }


        //Screen Recordatorio
        composable(VoxTaskScreen.Recordatorio.name) {
            val viewModelRecordatorio: RecordatorioViewModel = viewModel()
            val viewModelPlantilla: PlantillaBaseViewModel = viewModel()

            RecordatorioScreen(
                viewModelPlantilla = viewModelPlantilla,
                viewModel = viewModelRecordatorio,
                navController = navController
            )
        }




        //Screen Correo
        composable(VoxTaskScreen.Correo.name) {
            val viewModelCorreo: CorreoViewModel = viewModel()
            val viewModelPlantilla: PlantillaBaseViewModel = viewModel()

            CorreoScreen(
                viewModelPlantilla = viewModelPlantilla,
                viewModel = viewModelCorreo,
                navController = navController
            )
        }


        //Screen Ajustes
        composable(VoxTaskScreen.Ajustes.name) {
            val viewModelAjustes: AjustesViewModel = viewModel()
            AjustesScreen(
                viewModel = viewModelAjustes,
                navController = navController
            )
        }


        //Screen Perfil usuario logueado
        composable(VoxTaskScreen.Perfil.name) {
            val viewModelPerfil: PerfilViewModel = viewModel()
            val viewModelPlantilla: PlantillaBaseViewModel = viewModel()
            PerfilScreen(
                viewModelPlantilla = viewModelPlantilla,
                viewModel = viewModelPerfil,
                navController = navController
            )
        }
    }
}

