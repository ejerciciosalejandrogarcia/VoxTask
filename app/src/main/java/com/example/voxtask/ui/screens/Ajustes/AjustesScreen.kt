package com.example.voxtask.ui.screens.Ajustes
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.VoxTaskScreen
import com.example.voxtask.ui.theme.ColoresClaros
import com.example.voxtask.ui.theme.ColoresOscuros
import com.example.voxtask.ui.theme.LocalThemeManager
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.LocalTamanioPantalla
import com.example.voxtask.utils.LocalEspaciado
import com.example.voxtask.utils.TamanioPantalla
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.anchoMaximoContenido
import com.example.voxtask.utils.textoBody
import com.example.voxtask.utils.textoTitulo
import java.util.Locale
import android.Manifest
import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Share

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    plantillaBaseViewModel: PlantillaBaseViewModel,
    navController: NavController
) {
    val espaciado = LocalEspaciado.current
    val tamano = LocalTamanioPantalla.current
    val contexto = LocalContext.current
    val actividad = contexto as Activity
    val fondoUri by plantillaBaseViewModel.fondoUri.collectAsState()

    // — igual que VerificacionScreen —
    val anchoMaximo = tamano.anchoMaximoContenido

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { plantillaBaseViewModel.actualizarFondo(it) } }

    val permisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) imageLauncher.launch("image/*") }

    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    LaunchedEffect(Unit) { viewModel.inicializarIdioma(contexto) }

    PlantillaBase(
        viewModel = plantillaBaseViewModel,
        mostrarBotonInfo = false,
        navController = navController
    ) { paddingValues ->

        // Diálogos
        if (viewModel.mostrarSelectorColor) {
            ColorInterfazDialog(onDismiss = { viewModel.mostrarSelectorColor = false })
        }

        if (viewModel.mostrarSelectorVoz) {
            LaunchedEffect(Unit) { viewModel.cargarVoces(contexto) }
            AlertDialog(
                onDismissRequest = { viewModel.mostrarSelectorVoz = false },
                title = {
                    Text(
                        text = stringResource(R.string.selecciona_voz),
                        fontSize = tamano.textoTitulo,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (viewModel.vocesDisponibles.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_voces),
                            fontSize = tamano.textoBody
                        )
                    } else {
                        LazyColumn {
                            items(viewModel.vocesDisponibles) { voz ->
                                TextButton(
                                    onClick = { viewModel.aplicarVoz(voz.name, actividad) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = voz.locale.getDisplayName(Locale("es", "ES")),
                                        fontSize = tamano.textoBody,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.mostrarSelectorVoz = false }) {
                        Text(
                            text = stringResource(R.string.cancelar),
                            fontSize = tamano.textoBody
                        )
                    }
                }
            )
        }

        if (viewModel.mostrarSelectorIdioma) {
            AlertDialog(
                onDismissRequest = { viewModel.mostrarSelectorIdioma = false },
                title = {
                    Text(
                        text = stringResource(R.string.selecciona_idioma),
                        fontSize = tamano.textoTitulo,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn {
                        items(viewModel.idiomasDisponibles) { (codigo, nombre) ->
                            TextButton(
                                onClick = {
                                    viewModel.mostrarSelectorIdioma = false
                                    viewModel.cargarIdiomas(contexto, codigo) { actividad.recreate() }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = nombre,
                                    fontSize = tamano.textoBody,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.mostrarSelectorIdioma = false }) {
                        Text(
                            text = stringResource(R.string.cancelar),
                            fontSize = tamano.textoBody
                        )
                    }
                }
            )
        }

        // Contenedor con ancho máximo igual que VerificacionScreen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            val modificadorLista = if (anchoMaximo != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier
                    .widthIn(max = anchoMaximo)
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }

            LazyColumn(
                modifier = modificadorLista.padding(espaciado.l)
            ) {
                item {
                    OpcionAjuste(
                        icono = Icons.Default.RecordVoiceOver,
                        titulo = stringResource(R.string.cambiar_voz),
                        descripcion = buildString {
                            append("${stringResource(R.string.voz_actual)}: ${viewModel.vozActual}")
                            if (viewModel.idiomaVozActual.isNotEmpty()) {
                                append(" · ${viewModel.idiomaVozActual}")
                            }
                        },
                        tamanoTexto = tamano
                    ) { viewModel.mostrarSelectorVoz = true }
                }
                item {
                    OpcionAjuste(
                        icono = Icons.Default.Language,
                        titulo = stringResource(R.string.cambiar_idioma),
                        descripcion = "${stringResource(R.string.idioma_actual)}: ${viewModel.idiomaActual}",
                        tamanoTexto = tamano
                    ) { viewModel.mostrarSelectorIdioma = true }
                }
                item {
                    OpcionAjuste(
                        icono = Icons.Default.AccountCircle,
                        titulo = stringResource(R.string.mi_perfil),
                        descripcion = stringResource(R.string.ver_editar_perfil),
                        tamanoTexto = tamano
                    ) { navController.navigate(VoxTaskScreen.Perfil.name) }
                }
                item {
                    OpcionAjuste(
                        icono = Icons.Default.Palette,
                        titulo = stringResource(R.string.color_interfaz),
                        descripcion = stringResource(R.string.personaliza_colores),
                        tamanoTexto = tamano
                    ) { viewModel.mostrarSelectorColor = true }
                }
                item {
                    if (fondoUri != null) {
                        OpcionAjuste(
                            icono = Icons.Default.Wallpaper,
                            titulo = stringResource(R.string.quitar_fondo),
                            descripcion = stringResource(R.string.descripcion_quitar_fondo),
                            tamanoTexto = tamano
                        ) { plantillaBaseViewModel.actualizarFondo(null) }
                    } else {
                        OpcionAjuste(
                            icono = Icons.Default.Wallpaper,
                            titulo = stringResource(R.string.cambiar_fondo),
                            descripcion = stringResource(R.string.personaliza_fondo),
                            tamanoTexto = tamano
                        ) { permisos.launch(permiso) }
                    }
                }
                item {
                    OpcionAjuste(
                        icono = Icons.Default.Info,
                        titulo = stringResource(R.string.version_app),
                        descripcion = stringResource(R.string.ver_version_actual),
                        tamanoTexto = tamano
                    ) {
                        Toast.makeText(contexto, contexto.getString(R.string.version), Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    OpcionAjuste(
                        icono = Icons.Default.Share,
                        titulo = stringResource(R.string.compartir_app),
                        descripcion = stringResource(R.string.compartir_app_descripcion),
                        tamanoTexto = tamano
                    ) { viewModel.compartirAplicacion(contexto) }
                }
            }
        }
    }
}

@Composable
fun ColorInterfazDialog(onDismiss: () -> Unit) {
    val themeManager = LocalThemeManager.current
    val colorClaro by themeManager.colorClaro.collectAsState()
    val colorOscuro by themeManager.colorOscuro.collectAsState()
    val tamano = LocalTamanioPantalla.current
    val espaciado = LocalEspaciado.current

    val tamanoCirculo = when (tamano) {
        TamanioPantalla.COMPACTO  -> 40.dp
        TamanioPantalla.MEDIO     -> 48.dp
        TamanioPantalla.EXPANDIDO -> 56.dp
    }
    val altoGrid = when (tamano) {
        TamanioPantalla.COMPACTO  -> 100.dp
        TamanioPantalla.MEDIO     -> 120.dp
        TamanioPantalla.EXPANDIDO -> 150.dp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.opcion_color_interfaz),
                fontSize = tamano.textoTitulo,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            // La columna principal mantiene su scroll vertical por si la pantalla es muy pequeña
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ───────────────────────────────────────────────────────────
                // PALETA MODO CLARO (Deslizable Horizontalmente)
                // ───────────────────────────────────────────────────────────
                Text(
                    text = stringResource(R.string.opcion_color_interfaz_modo_claro),
                    fontSize = tamano.textoBody,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = espaciado.s)
                )

                // Usamos una Row con horizontalScroll y un estado de scroll independiente para cada paleta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()) // <- Esto activa el scroll horizontal
                        .padding(vertical = espaciado.xs),
                    horizontalArrangement = Arrangement.spacedBy(espaciado.xs) // Espaciado automático entre círculos
                ) {
                    ColoresClaros.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(tamanoCirculo)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { themeManager.setColorClaro(color) }
                                .then(
                                    if (color == colorClaro)
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(espaciado.l))

                // ───────────────────────────────────────────────────────────
                // PALETA MODO OSCURO (Deslizable Horizontalmente)
                // ───────────────────────────────────────────────────────────
                Text(
                    text = stringResource(R.string.opcion_color_interfaz_modo_oscuro),
                    fontSize = tamano.textoBody,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = espaciado.s)
                )

                // Segunda Row con su propio scroll independiente
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()) // <- Esto activa el scroll horizontal
                        .padding(vertical = espaciado.xs),
                    horizontalArrangement = Arrangement.spacedBy(espaciado.xs)
                ) {
                    ColoresOscuros.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(tamanoCirculo)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { themeManager.setColorOscuro(color) }
                                .then(
                                    if (color == colorOscuro)
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.model_cerrar),
                    fontSize = tamano.textoBody,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpcionAjuste(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    tamanoTexto: TamanioPantalla,          // ← nuevo parámetro
    onClick: () -> Unit
) {
    val espaciado = LocalEspaciado.current

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = espaciado.s),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = titulo,
                    fontSize = tamanoTexto.textoBody,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = descripcion,
                    fontSize = tamanoTexto.textoBody
                )
            },
            leadingContent = {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(
                        when (tamanoTexto) {
                            TamanioPantalla.COMPACTO  -> 24.dp
                            TamanioPantalla.MEDIO     -> 28.dp
                            TamanioPantalla.EXPANDIDO -> 32.dp
                        }
                    )
                )
            }
        )
    }
}