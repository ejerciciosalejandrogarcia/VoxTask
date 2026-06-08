package com.example.voxtask

import android.app.Activity
import android.content.Intent
import android.os.Build
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
import androidx.navigation.navArgument
import com.example.voxtask.ui.screens.Ajustes.AjustesScreen
import com.example.voxtask.ui.screens.Ajustes.AjustesViewModel
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContrasenaScreen
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContraseniaViewModel
import com.example.voxtask.ui.screens.Clima.ClimaScreen
import com.example.voxtask.ui.screens.Clima.ClimaViewModel
import com.example.voxtask.ui.screens.Contador.ContadorScreen
import com.example.voxtask.ui.screens.VerCorreo.VerCorreoScreen
import com.example.voxtask.ui.screens.Contador.ContadorViewModel
import com.example.voxtask.ui.screens.Correo.CorreoScreen
import com.example.voxtask.ui.screens.Correo.CorreoViewModel
import com.example.voxtask.ui.screens.EnviarCorreo.EnviarCorreoScreen
import com.example.voxtask.ui.screens.EnviarCorreo.EnviarCorreoViewModel
import com.example.voxtask.ui.screens.Lista_Compra.ListaCompraScreen
import com.example.voxtask.ui.screens.Lista_Compra.ListaCompraViewModel
import com.example.voxtask.ui.screens.Inicio.InicioScreen
import com.example.voxtask.ui.screens.Inicio.InicioViewModel
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionScreen
import com.example.voxtask.ui.screens.Inicio_Sesion.InicioSesionViewModel
import com.example.voxtask.ui.screens.NuevaContrasenia.NuevaContraseniaScreen
import com.example.voxtask.ui.screens.Perfil.PerfilScreen
import com.example.voxtask.ui.screens.Perfil.PerfilViewModel
import com.example.voxtask.ui.screens.Recordatorio.RecordatorioScreen
import com.example.voxtask.ui.screens.Recordatorio.RecordatorioViewModel
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioScreen
import com.example.voxtask.ui.screens.Registro_Usuario.RegistroUsuarioViewModel
import com.example.voxtask.ui.screens.VerCorreo.VerCorreoViewModel
import com.example.voxtask.ui.screens.Verificacion.VerificacionScreen
import com.example.voxtask.ui.screens.Verificacion.VerificacionViewModel
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.ProveedorAdaptativo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

/**
 * Define las rutas disponibles en la navegación de la aplicación
 */
enum class VoxTaskScreen {
    Inicio_sesion,
    Registro_Usuario,
    Inicio,
    Contador,
    ListaCompra,
    Recordatorio,
    Correo,
    Ajustes,
    Perfil,
    EnviarCorreo,
    VerCorreo,
    CambiarContrasenia,
    Verificacion,
    RegistrarNuevaContrasenia,
    Clima
}

/**
 * Esta funcion es el componente principal que coordina la navegación,
 * los estados de autenticación y la adaptación de la aplicación.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VoxTaskApp(
    windowSize: WindowWidthSizeClass,
    plantillaBaseViewModel: PlantillaBaseViewModel,
    onNavControllerReady: (NavController) -> Unit = {},
    deepLinkIntent: Intent? = null
) {
    ProveedorAdaptativo(tamanioPantalla = windowSize) {
        /** Variables */
        val navController = rememberNavController()
        val contexto = LocalContext.current
        val viewModelInicioSesion: InicioSesionViewModel = viewModel()
        val viewModelRegistrar: RegistroUsuarioViewModel = viewModel()
        val viewModelInicio: InicioViewModel = viewModel()

        /** Procesa un enlace externo para la recuperación de contraseñas */
        LaunchedEffect(deepLinkIntent) {
            val datos = deepLinkIntent?.data
            if (datos?.scheme == "voxtask" && datos.host == "nuevacontrasena") {
                val codigoOob = datos.getQueryParameter("oobCode") ?: ""
                navController.navigate("${VoxTaskScreen.RegistrarNuevaContrasenia.name}?oobCode=$codigoOob")
            }
        }
        /**
         * Permite exponer el navController hacia el exterior una vez inicializado,
         * permitiendo que componentes externos, puedan controlar la navegación de la aplicación
         */
        LaunchedEffect(navController) {
            onNavControllerReady(navController)
        }

        /** Define el manejador del resultado del inicio de sesión con Google */
        val lanzadorGoogle = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
                try {
                    val cuenta = tarea.getResult(ApiException::class.java)
                    viewModelInicioSesion.autenticarConGoogle(
                        tokenGoogle = cuenta.idToken!!,
                        codigoAutorizacion = cuenta.serverAuthCode
                    )
                } catch (e: ApiException) {
                }
            } else {
            }
        }
        /**
         * Establece las rutas y las transiciones entre las distintas pantallas.
         */
        NavHost(
            navController = navController,
            startDestination = VoxTaskScreen.Inicio_sesion.name
        ) {

            composable(route = VoxTaskScreen.Inicio_sesion.name) {
                InicioSesionScreen(
                    alIniciarSesionExitosamente = {
                        navController.navigate(VoxTaskScreen.Inicio.name) {
                            popUpTo(VoxTaskScreen.Inicio_sesion.name) { inclusive = true }
                        }
                    },
                    alNavegarARegistro = { destino -> navController.navigate(destino) },
                    alPulsarGoogle = {
                        val clienteGoogle = viewModelInicioSesion.obtenerClienteGoogle(contexto)
                        lanzadorGoogle.launch(clienteGoogle.signInIntent)
                    },
                    viewModel = viewModelInicioSesion
                )
            }

            composable(VoxTaskScreen.Registro_Usuario.name) {
                RegistroUsuarioScreen(
                    alRegistroExitoso = {
                        navController.navigate(VoxTaskScreen.Inicio_sesion.name) {
                            popUpTo(VoxTaskScreen.Registro_Usuario.name) { inclusive = true }
                        }
                        viewModelRegistrar.limpiarEstadoRegistro()
                    },
                    viewModel = viewModelRegistrar
                )
            }

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
                viewModelInicio.abrirClima = {
                    navController.navigate(VoxTaskScreen.Clima.name) {
                        popUpTo(VoxTaskScreen.Inicio.name)
                    }
                }
                InicioScreen(
                    viewModel = viewModelInicio,
                    navController = navController,
                    plantillaBaseViewModel = plantillaBaseViewModel
                )
            }

            composable(VoxTaskScreen.Contador.name) {
                val viewModelContador: ContadorViewModel = viewModel()
                ContadorScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelContador,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.ListaCompra.name) {
                val viewModelListaCompra: ListaCompraViewModel = viewModel()
                ListaCompraScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelListaCompra,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.Recordatorio.name) {
                val viewModelRecordatorio: RecordatorioViewModel = viewModel()
                RecordatorioScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelRecordatorio,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.Correo.name) {
                val viewModelCorreo: CorreoViewModel = viewModel()
                CorreoScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelCorreo,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.EnviarCorreo.name) {
                val viewModelEnviarCorreo: EnviarCorreoViewModel = viewModel()
                EnviarCorreoScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelEnviarCorreo,
                    navController = navController
                )
            }

            composable("${VoxTaskScreen.VerCorreo.name}/{correoId}") { entradaPila ->
                val viewModelVerCorreo: VerCorreoViewModel = viewModel()
                val correoId = entradaPila.arguments?.getString("correoId")
                VerCorreoScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelVerCorreo,
                    navController = navController,
                    correoId = correoId
                )
            }

            composable(VoxTaskScreen.Ajustes.name) {
                val viewModelAjustes: AjustesViewModel = viewModel()
                AjustesScreen(
                    viewModel = viewModelAjustes,
                    plantillaBaseViewModel = plantillaBaseViewModel,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.Perfil.name) {
                val viewModelPerfil: PerfilViewModel = viewModel()
                PerfilScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelPerfil,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.Verificacion.name) {
                val viewModelVerificacion: VerificacionViewModel = viewModel()
                VerificacionScreen(
                    viewModel = viewModelVerificacion,
                    navController = navController
                )
            }

            composable(VoxTaskScreen.CambiarContrasenia.name) { entradaPila ->
                val viewModel: CambiarContraseniaViewModel = viewModel(entradaPila)
                CambiarContrasenaScreen(navController, viewModel)
            }

            composable(
                route = "${VoxTaskScreen.RegistrarNuevaContrasenia.name}?oobCode={oobCode}",
                arguments = listOf(navArgument("oobCode") { defaultValue = "" })
            ) { entradaPila ->
                val codigoOob = entradaPila.arguments?.getString("oobCode") ?: ""
                val viewModel: CambiarContraseniaViewModel = viewModel()
                NuevaContraseniaScreen(navController, viewModel, codigoOob)
            }

            composable(VoxTaskScreen.Clima.name) {
                val viewModelClima: ClimaViewModel = viewModel()
                ClimaScreen(
                    viewModelPlantilla = plantillaBaseViewModel,
                    viewModel = viewModelClima,
                    navController = navController
                )
            }
        }
    }
}