package com.example.voxtask.ui.screens.Correo

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: CorreoViewModel,
    navController: NavController
) {
    //Variables
    val contexto = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            try {
                val cuenta = GoogleSignIn
                    .getSignedInAccountFromIntent(resultado.data)
                    .getResult(ApiException::class.java)
                viewModel.guardarTokenYCargarCorreos(contexto, cuenta.serverAuthCode)
            } catch (e: ApiException) {

            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar(contexto)
    }

    PlantillaBase( viewModel=viewModelPlantilla,navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val estado = uiState) {

                is CorreoUiState.Cargando -> {
                    CircularProgressIndicator()
                }

                is CorreoUiState.NecesitaConectarGoogle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Conecta tu cuenta de Gmail para ver tus correos",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = {
                                val cliente = viewModel.obtenerClienteGoogle(contexto)
                                lanzadorGoogle.launch(cliente.signInIntent)
                            }
                        ) {
                            Text("Conectar Gmail")
                        }
                    }
                }

                is CorreoUiState.Exito -> {
                    if (estado.correos.isEmpty()) {
                        Text("No tienes correos.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(estado.correos) { correo ->
                                TarjetaCorreo(correo)
                            }
                        }
                    }
                }

                is CorreoUiState.Error -> {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaCorreo(correo: com.example.voxtask.databases.model.Correo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = correo.asunto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "De: ${correo.remitente}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = correo.fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}