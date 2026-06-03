package com.example.voxtask.ui.screens.Correo

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.example.voxtask.R
import kotlinx.coroutines.launch
/**
 * Pantalla principal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: CorreoViewModel,
    navController: NavController
) {
    /** Variables */
    val contexto = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val uiState by viewModel.uiState.collectAsState()
    val paddingContenido = dimensionResource(R.dimen.correo_padding_contenido)
    val tamanoBotonCrear = dimensionResource(R.dimen.correo_boton_crear)
    val tamanoIconoCrear = dimensionResource(R.dimen.correo_icono_crear)
    val anchoMaximoContenido = tamano.anchoMaximoContenido
    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            try {
                val cuenta = GoogleSignIn
                    .getSignedInAccountFromIntent(resultado.data)
                    .getResult(ApiException::class.java)
                viewModel.guardarTokenYCargarCorreos(contexto, cuenta.serverAuthCode)
            } catch (e: ApiException) { }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /** SnackBar */
    LaunchedEffect(Unit) {
        launch {
            viewModel.errorFlow.collect { mensaje ->
                android.util.Log.d("SNACKBAR_TEST", "Error recibido: $mensaje")
                snackbarHostState.showSnackbar(
                    message = mensaje,
                    duration = SnackbarDuration.Short
                )
            }
        }
        viewModel.iniciar(contexto)
    }



    PlantillaBase(viewModel = viewModelPlantilla, navController = navController) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(paddingContenido)
        ) {

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = espaciado.xl)
                    .zIndex(10f)
            )

            val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximoContenido)
                    .fillMaxSize()
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Box(
                modifier = modificadorContenido,
                contentAlignment = Alignment.Center
            ) {
                when (val estado = uiState) {
                    /** Pantalla a la hora de cargar el correo */
                    is CorreoUiState.Cargando -> {
                        CircularProgressIndicator()
                    }
                    /** Pantalla a la hora de que accedas a la aplicacion sin una cuenta de google */
                    is CorreoUiState.NecesitaConectarGoogle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(paddingContenido)
                        ) {
                            Text(
                                text = stringResource(R.string.txt_title_advertencia_ver_correos),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = espaciado.xl)
                            )
                            Button(
                                onClick = {
                                    val cliente = viewModel.obtenerClienteGoogle(contexto)
                                    lanzadorGoogle.launch(cliente.signInIntent)
                                }
                            ) {
                                Text(stringResource(R.string.txt_btn_conectar_gmail))
                            }
                        }
                    }
                    /** Pantalla a la hora de que haya correos */
                    is CorreoUiState.Exito -> {
                        if (estado.correos.isEmpty()) {
                            Text(stringResource(R.string.txt_title_no_correos))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(espaciado.s)
                            ) {
                                items(estado.correos) { correo ->
                                    TarjetaCorreo(correo, navController = navController)
                                }
                            }
                        }
                    }
                    /** Pantalla a la hora de que hubo un error al cargar el correo */
                    is CorreoUiState.Error -> {
                        if (estado.esErrorDeCarga) {
                            Button(onClick = { viewModel.iniciar(contexto) }) {
                                Text(stringResource(R.string.txt_btn_reintentar_cargar_correos))
                            }
                        }
                    }
                }

                /** Boton para enviar un nuevo correo */
                Button(
                    onClick = { navController.navigate(VoxTaskScreen.EnviarCorreo.name) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = espaciado.s, end = espaciado.xs)
                        .size(tamanoBotonCrear),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.txt_btne_crear_correo),
                        tint = Color.White,
                        modifier = Modifier.size(tamanoIconoCrear)
                    )
                }
            }
        }
    }
}
/**
 * Esta funcion crea las tarjetas de cada gmail con el asunto del mensaje, remitente,fecha y hora
 */
@Composable
fun TarjetaCorreo(
    correo: com.example.voxtask.databases.model.Correo,
    navController: NavController
) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current

    val paddingTarjeta = when (tamano) {
        TamanioPantalla.COMPACTO  -> espaciado.m
        TamanioPantalla.MEDIO     -> espaciado.l
        TamanioPantalla.EXPANDIDO -> espaciado.xl
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("${VoxTaskScreen.VerCorreo.name}/${correo.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(paddingTarjeta)) {
            Text(
                text = correo.asunto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = tamano.textoBody
            )
            Text(
                text = "De: ${correo.remitente}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = espaciado.xs)
            )
            Text(
                text = correo.fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = espaciado.xs / 2)
            )
        }
    }
}