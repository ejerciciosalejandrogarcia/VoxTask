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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.databases.model.Usuario
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.voxtask.R
import com.example.voxtask.utils.anchoMaximoContenido

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContadorScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: ContadorViewModel,
    navController: NavController
) {
    val contexto      = LocalContext.current
    val espaciado     = LocalEspaciado.current
    val tamano        = LocalTamanioPantalla.current
    val configuracion = LocalConfiguration.current
    val usuario       = FirebaseAuth.getInstance().currentUser
    val uid           = usuario?.uid

    val esLandscape = configuracion.screenWidthDp > configuracion.screenHeightDp

    val tamanoCirculo = if (esLandscape) {
        dimensionResource(R.dimen.contador_circulo_landscape)
    } else {
        dimensionResource(R.dimen.contador_circulo)
    }
    val tamanoBoton      = dimensionResource(R.dimen.contador_boton)
    val tamanoIconoBoton = dimensionResource(R.dimen.contador_icono_boton)

    val anchoMaximoContenido = tamano.anchoMaximoContenido

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        viewModel.restaurarSiServicioActivo()
        viewModel.comprobarEstadoService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lanzadorPermiso.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel.mostrarContador) {
        if (viewModel.mostrarContador) {
            val idioma  = TextoAVoz.localeActual.language
            val mensaje = when (idioma) {
                "en" -> "Creating and starting timer."
                "fr" -> "Création et démarrage du compteur."
                "de" -> "Timer wird erstellt und gestartet."
                "it" -> "Creazione e avvio del contatore."
                "pt" -> "Criando e iniciando o contador."
                else -> "Creando e iniciando contador."
            }
            TextoAVoz.hablar(contexto, mensaje)
        }
    }

    LaunchedEffect(uid) {
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
            val idioma  = TextoAVoz.localeActual.language
            val mensaje = when (idioma) {
                "en" -> "Okay $nombre, how much time do you want to set on the timer?"
                "fr" -> "D'accord $nombre, combien de temps voulez-vous mettre sur le compteur?"
                "de" -> "Okay $nombre, wie viel Zeit möchten Sie für den Timer einstellen?"
                "it" -> "Okay $nombre, quanto tempo vuoi impostare sul timer?"
                "pt" -> "Ok $nombre, quanto tempo você quer colocar no contador?"
                else -> "¿Okey $nombre cuanto tiempo quieres poner al contador?"
            }
            TextoAVoz.hablar(contexto, mensaje)
        }
    }

    PlantillaBase(
        viewModel         = viewModelPlantilla,
        navController     = navController,
        textoInformacion  = stringResource(R.string.txt_info_contador),
        onTextoReconocido = { texto -> viewModel.onTextoRecibido(texto, contexto) }
    ) { paddingValues ->
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(visible = viewModel.mostrarContador) {

                val modificadorContenido =
                    if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier.widthIn(max = anchoMaximoContenido)
                    } else {
                        Modifier
                    }

                Column(
                    modifier = modificadorContenido
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = espaciado.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── Círculo con el tiempo ─────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(tamanoCirculo)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Text(
                            text       = viewModel.tiempoFormato,
                            style      = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(espaciado.xl))

                    // ── Botones ───────────────────────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(espaciado.l),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {

                        // ── Botón Play / Pause
                        // Deshabilitado cuando el contador ha terminado (terminado == true)
                        IconButton(
                            onClick  = {
                                if (viewModel.corriendo) {
                                    viewModel.parar(contexto)
                                } else {
                                    viewModel.iniciar(contexto)
                                }
                            },
                            enabled  = !viewModel.terminado,
                            modifier = Modifier
                                .size(tamanoBoton)
                                .clip(CircleShape)
                                .background(
                                    // Gris cuando está deshabilitado, verde en caso contrario
                                    if (!viewModel.terminado) VerdePrimario else Color.Gray
                                )
                        ) {
                            Icon(
                                imageVector = if (viewModel.corriendo)
                                    Icons.Default.Pause
                                else
                                    Icons.Default.PlayArrow,
                                contentDescription = if (viewModel.corriendo)
                                    stringResource(R.string.btn_parar)
                                else
                                    stringResource(R.string.btn_iniciar),
                                tint     = Color.White,
                                modifier = Modifier.size(tamanoIconoBoton)
                            )
                        }

                        // ── Botón Cancelar (X)
                        // Siempre activo: al pulsarlo se oculta el contador y para el sonido
                        IconButton(
                            onClick  = { viewModel.cancelar(contexto) },
                            modifier = Modifier
                                .size(tamanoBoton)
                                .clip(CircleShape)
                                .background(Color.Red)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_cancelar),
                                tint               = Color.White,
                                modifier           = Modifier.size(tamanoIconoBoton)
                            )
                        }
                    }
                }
            }
        }
    }
}