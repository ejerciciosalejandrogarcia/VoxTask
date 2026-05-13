package com.example.voxtask.ui.screens.Contador

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.voxtask.R
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContadorScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: ContadorViewModel,
    navController: NavController
) {
    val contexto = LocalContext.current
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid = usuario?.uid

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // Restaurar estado si el servicio sigue activo
    LaunchedEffect(Unit) {
        viewModel.restaurarSiServicioActivo()
        viewModel.comprobarEstadoService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lanzadorPermiso.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel.mostrarContador) {
        if (viewModel.mostrarContador) {
            TextoAVoz.hablar(contexto, "Creando e iniciando contador.")
        }
    }

    LaunchedEffect(uid) {
        // Solo habla si NO hay un contador activo ya
        if (!viewModel.mostrarContador) {
            val nombre = if (uid != null) {
                try {
                    val doc = FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .get()
                        .await()
                    val usuarioObj = doc.toObject(Usuario::class.java)
                    usuarioObj?.nombre ?: "Usuario"
                } catch (e: Exception) {
                    "Usuario"
                }
            } else {
                "Usuario"
            }
            TextoAVoz.hablar(contexto, "¿Okey $nombre cuanto tiempo quieres poner al contador?.")
        }
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.onTextoRecibido(texto, contexto) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = viewModel.mostrarContador) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(VerdePrimario.copy(alpha = 0.1f))
                                .border(3.dp, VerdePrimario, CircleShape)
                        ) {
                            Text(
                                text = viewModel.tiempoFormato,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = VerdePrimario
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (viewModel.corriendo) {
                                        viewModel.parar(contexto)
                                    } else {
                                        viewModel.iniciar()
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(VerdePrimario)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.corriendo)
                                        Icons.Default.Pause
                                    else
                                        Icons.Default.PlayArrow,
                                    contentDescription = if (viewModel.corriendo) stringResource(R.string.btn_parar) else stringResource(R.string.btn_iniciar),
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.cancelar(contexto)
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.btn_cancelar),
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}