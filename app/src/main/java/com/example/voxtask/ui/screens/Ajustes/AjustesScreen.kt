package com.example.voxtask.ui.screens.Ajustes

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
import com.example.voxtask.utils.textoTitulo
import java.util.Locale
import android.Manifest
import android.os.Build
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
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            plantillaBaseViewModel.actualizarFondo(it)
        }
    }

    val permisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imageLauncher.launch("image/*")
        }
    }
    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    LaunchedEffect(Unit) {
        viewModel.inicializarIdioma(contexto)
    }

    PlantillaBase(
        viewModel = plantillaBaseViewModel,
        mostrarBotonInfo = false,
        navController = navController
    ) { paddingValues ->

        // Dialogos
        if (viewModel.mostrarSelectorColor) {
            ColorInterfazDialog(
                onDismiss = { viewModel.mostrarSelectorColor = false }
            )
        }
        if (viewModel.mostrarSelectorVoz) {
            LaunchedEffect(Unit) {
                viewModel.cargarVoces(contexto)
            }
            AlertDialog(
                onDismissRequest = { viewModel.mostrarSelectorVoz = false },
                title = { Text(stringResource(R.string.selecciona_voz)) },
                text = {
                    if (viewModel.vocesDisponibles.isEmpty()) {
                        Text(stringResource(R.string.no_voces))
                    } else {
                        LazyColumn {
                            items(viewModel.vocesDisponibles) { voz ->
                                TextButton(
                                    onClick = { viewModel.aplicarVoz(voz.name, actividad) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = voz.locale.getDisplayName(Locale("es", "ES")),
                                        style = MaterialTheme.typography.labelSmall,
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
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }

        if (viewModel.mostrarSelectorIdioma) {
            AlertDialog(
                onDismissRequest = { viewModel.mostrarSelectorIdioma = false },
                title = { Text(stringResource(R.string.selecciona_idioma)) },
                text = {
                    LazyColumn {
                        items(viewModel.idiomasDisponibles) { (codigo, nombre) ->
                            TextButton(
                                onClick = {
                                    viewModel.mostrarSelectorIdioma = false
                                    viewModel.cargarIdiomas(contexto, codigo) {
                                        actividad.recreate()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = nombre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.mostrarSelectorIdioma = false }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(espaciado.l)                          // antes: 16.dp
        ) {
            item {
                Text(
                    text = stringResource(R.string.ajustes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = tamano.textoTitulo,             // antes: sin adaptar
                    modifier = Modifier.padding(bottom = espaciado.l)  // antes: 16.dp
                )
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.RecordVoiceOver,
                    titulo = stringResource(R.string.cambiar_voz),
                    descripcion = "${stringResource(R.string.voz_actual)}: ${viewModel.vozActual}"
                ) { viewModel.mostrarSelectorVoz = true }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Language,
                    titulo = stringResource(R.string.cambiar_idioma),
                    descripcion = "${stringResource(R.string.idioma_actual)}: ${viewModel.idiomaActual}"
                ) { viewModel.mostrarSelectorIdioma = true }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.AccountCircle,
                    titulo = stringResource(R.string.mi_perfil),
                    descripcion = stringResource(R.string.ver_editar_perfil)
                ) { navController.navigate(VoxTaskScreen.Perfil.name) }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Palette,
                    titulo = stringResource(R.string.color_interfaz),
                    descripcion = stringResource(R.string.personaliza_colores)
                ) {
                    viewModel.mostrarSelectorColor = true
                }
            }
            item {
                if (fondoUri != null) {
                    OpcionAjuste(
                        icono = Icons.Default.Wallpaper,
                        titulo = stringResource(R.string.quitar_fondo),
                        descripcion = stringResource(R.string.descripcion_quitar_fondo)
                    ) {
                        plantillaBaseViewModel.actualizarFondo(null)
                    }
                } else {
                    OpcionAjuste(
                        icono = Icons.Default.Wallpaper,
                        titulo = stringResource(R.string.cambiar_fondo),
                        descripcion = stringResource(R.string.personaliza_fondo)
                    ) {
                        permisos.launch(permiso)
                    }
                }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Info,
                    titulo = stringResource(R.string.version_app),
                    descripcion = stringResource(R.string.ver_version_actual)
                ) {
                    Toast.makeText(contexto, contexto.getString(R.string.version), Toast.LENGTH_SHORT).show()
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
    val esModoOscuro = isSystemInDarkTheme()
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
                stringResource(R.string.opcion_color_interfaz),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Selector color modo claro
                Text(
                    stringResource(R.string.opcion_color_interfaz_modo_claro),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = espaciado.s)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(altoGrid)
                ) {
                    items(ColoresClaros) { color ->
                        Box(
                            modifier = Modifier
                                .padding(espaciado.xs)
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

                // Selector color modo oscuro
                Text(
                    stringResource(R.string.opcion_color_interfaz_modo_oscuro),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = espaciado.s) // antes: 8.dp
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(altoGrid)
                ) {
                    items(ColoresOscuros) { color ->
                        Box(
                            modifier = Modifier
                                .padding(espaciado.xs)
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
                Text(stringResource(R.string.model_cerrar), color = MaterialTheme.colorScheme.primary)
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
            headlineContent = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(descripcion, style = MaterialTheme.typography.bodySmall) },
            leadingContent = {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}