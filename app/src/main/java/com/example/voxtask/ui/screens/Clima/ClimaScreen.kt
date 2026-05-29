package com.example.voxtask.ui.screens.Clima

import androidx.compose.material3.MaterialTheme
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.example.voxtask.R
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.textoTitulo
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.anchoMaximoContenido
import androidx.compose.ui.graphics.compositeOver

@SuppressLint("MissingPermission", "LocalContextGetResourceValueCall")
@Composable
fun ClimaScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: ClimaViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val uiState by viewModel.uiState.collectAsState()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val anchoMaximoContenido = tamano.anchoMaximoContenido
    val snackbarHostState = remember { SnackbarHostState() }

    val paddingHorizontalPantalla = dimensionResource(R.dimen.cambiar_contrasena_padding_card_horizontal)
    val paddingVerticalPantalla = dimensionResource(R.dimen.cambiar_contrasena_padding_card_vertical)
    val tamanoIconoClimaPrincipal = dimensionResource(R.dimen.cambiar_contrasena_icono_email)

    // ✅ Función reutilizable para obtener ubicación actual
    fun obtenerUbicacionYCargar() {
        val solicitud = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedClient.getCurrentLocation(solicitud, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModel.cargarClima(loc.latitude, loc.longitude)
                } else {
                    viewModel.establecerSinUbicacion()
                }
            }
            .addOnFailureListener {
                viewModel.establecerSinUbicacion()
            }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            obtenerUbicacionYCargar() // ✅ Usa getCurrentLocation
        } else {
            viewModel.establecerSinUbicacion()
        }
    }

    LaunchedEffect(Unit) {
        val permiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionYCargar() // ✅ También aquí
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- ESCUCHA DE ERRORES ---
    LaunchedEffect(uiState.mensajeErrorResId, uiState.errorMensajeDinamico) {
        if (uiState.mensajeErrorResId != null) {
            val mensajeFinal = if (uiState.errorMensajeDinamico != null) {
                context.getString(uiState.mensajeErrorResId!!, uiState.errorMensajeDinamico)
            } else {
                context.getString(uiState.mensajeErrorResId!!)
            }
            snackbarHostState.showSnackbar(
                message = mensajeFinal,
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }

    // --- LÓGICA DE DEGRADADOS DINÁMICOS ---
    val colorArribaPorDefecto = MaterialTheme.colorScheme.surface
    val colorAbajoPorDefecto = MaterialTheme.colorScheme.surfaceVariant

    val (colorArriba, colorAbajo, iconoClima) = if (uiState.datos != null) {
        val datos = uiState.datos!!
        val c = datos.codigo
        when {
            !datos.es_de_dia -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Icons.Default.NightsStay)
            c == 1063 || c in 1180..1246 || c in 1273..1282 -> Triple(Color(0xFF475569), Color(0xFF64748B), Icons.Default.WaterDrop)
            c == 1030 || c == 1135 || c == 1147 -> Triple(Color(0xFF64748B), Color(0xFF94A3B8), Icons.Default.BlurOn)
            datos.temperatura < 12.0 || c == 1066 || c in 1210..1258 -> Triple(Color(0xFF1E3A8A), Color(0xFF3B82F6), Icons.Default.AcUnit)
            datos.temperatura >= 30.0 -> Triple(Color(0xFFEA580C), Color(0xFFFBBF24), Icons.Default.WbSunny)
            else -> Triple(Color(0xFF0284C7), Color(0xFF38BDF8), Icons.Default.CloudQueue)
        }
    } else {
        Triple(colorArribaPorDefecto, colorAbajoPorDefecto, Icons.Default.Cloud)
    }

    val animadoArriba by animateColorAsState(targetValue = colorArriba, animationSpec = tween(1000), label = "animArriba")
    val animadoAbajo by animateColorAsState(targetValue = colorAbajo, animationSpec = tween(1000), label = "animAbajo")

    val colorDeContenido = if (uiState.datos != null) Color.White else MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize()) {

        PlantillaBase(
            viewModel = viewModelPlantilla,
            navController = navController,
            textoInformacion = stringResource(R.string.clima_info_plantilla),
            onTextoReconocido = {}
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(animadoArriba, animadoAbajo)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(horizontal = paddingHorizontalPantalla, vertical = paddingVerticalPantalla),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val modificadorContenido = if (anchoMaximoContenido != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier
                            .widthIn(max = anchoMaximoContenido)
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }

                    Column(
                        modifier = modificadorContenido,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when {
                            uiState.estaCargando && uiState.datos == null -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(espaciado.m))
                                    Text(
                                        text = stringResource(R.string.clima_cargando),
                                        fontSize = tamano.textoBody,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            uiState.sinUbicacion && uiState.datos == null -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOff,
                                        contentDescription = stringResource(R.string.desc_clima_sin_ubicacion),
                                        modifier = Modifier.size(64.dp),
                                        tint = colorDeContenido.copy(alpha = 0.4f)
                                    )
                                    Spacer(Modifier.height(espaciado.m))
                                    Text(
                                        text = stringResource(R.string.clima_sin_ubicacion),
                                        fontSize = tamano.textoBody,
                                        color = colorDeContenido.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(espaciado.m))
                                    // ✅ Botón corregido: lanza el permiso, cargarClima se llama en el callback
                                    Button(
                                        onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(text = stringResource(R.string.clima_btn_permitir), fontSize = tamano.textoBody)
                                    }
                                }
                            }

                            uiState.datos != null -> {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    TarjetaClimaContenido(
                                        datos = uiState.datos!!,
                                        colorTexto = colorDeContenido,
                                        iconoClima = iconoClima,
                                        tamano = tamano,
                                        espaciado = espaciado,
                                        tamanoIconoClima = tamanoIconoClimaPrincipal
                                    )
                                    if (uiState.estaCargando) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(4.dp),
                                            strokeWidth = 2.dp,
                                            color = colorDeContenido
                                        )
                                    }
                                }
                            }

                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { obtenerUbicacionYCargar() }, // ✅ También corregido aquí
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.clima_btn_reintentar),
                                            fontSize = tamano.textoBody
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
                .padding(horizontal = 16.dp)
                .zIndex(999f)
        )
    }
}

@Composable
private fun TarjetaClimaContenido(
    datos: com.example.voxtask.databases.network.ClimaResponse,
    colorTexto: Color,
    iconoClima: androidx.compose.ui.graphics.vector.ImageVector,
    tamano: TamanioPantalla,
    espaciado: com.example.voxtask.utils.Espaciado,
    tamanoIconoClima: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(espaciado.m)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(espaciado.s)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.desc_clima_marcador_posicion),
                    tint = colorTexto,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = datos.municipio,
                    fontSize = (tamano.textoTitulo.value + 4).sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
            Text(
                text = datos.region,
                fontSize = tamano.textoBody,
                color = colorTexto.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(espaciado.s))

        Icon(
            imageVector = iconoClima,
            contentDescription = stringResource(R.string.desc_clima_icono_principal),
            modifier = Modifier.size(tamanoIconoClima),
            tint = colorTexto
        )

        Text(
            text = "${datos.temperatura}${datos.unidad_temp}",
            fontSize = (tamano.textoTitulo.value + 36).sp,
            fontWeight = FontWeight.Black,
            color = colorTexto
        )

        Text(
            text = datos.texto_clima,
            fontSize = (tamano.textoBody.value + 2).sp,
            fontWeight = FontWeight.Medium,
            color = colorTexto.copy(alpha = 0.9f)
        )

        HorizontalDivider(
            color = colorTexto.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = espaciado.s, horizontal = espaciado.m)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DatoClimaItem(
                icono = Icons.Default.Thermostat,
                valor = "${datos.sensacion_termica}°C",
                etiqueta = stringResource(R.string.clima_etiqueta_sensacion),
                colorTexto = colorTexto,
                tamano = tamano,
                descripcionIcono = stringResource(R.string.desc_clima_icono_sensacion),
                espaciadoIntermedio = espaciado.s
            )
            DatoClimaItem(
                icono = Icons.Default.Air,
                valor = "${datos.viento} ${datos.unidad_viento}",
                etiqueta = stringResource(R.string.clima_etiqueta_viento),
                colorTexto = colorTexto,
                tamano = tamano,
                descripcionIcono = stringResource(R.string.desc_clima_icono_viento),
                espaciadoIntermedio = espaciado.s
            )
            DatoClimaItem(
                icono = Icons.Default.WaterDrop,
                valor = "${datos.humedad}%",
                etiqueta = stringResource(R.string.clima_etiqueta_humedad),
                colorTexto = colorTexto,
                tamano = tamano,
                descripcionIcono = stringResource(R.string.desc_clima_icono_humedad),
                espaciadoIntermedio = espaciado.s
            )
        }
    }
}

@Composable
private fun DatoClimaItem(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    etiqueta: String,
    colorTexto: Color,
    tamano: TamanioPantalla,
    descripcionIcono: String,
    espaciadoIntermedio: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(espaciadoIntermedio)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcionIcono,
            tint = colorTexto.copy(alpha = 0.9f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = valor,
            color = colorTexto,
            fontSize = tamano.textoBody,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = etiqueta,
            fontSize = (tamano.textoBody.value - 2).sp,
            color = colorTexto.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}