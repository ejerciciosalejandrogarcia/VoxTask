package com.example.voxtask.ui.screens.Ajustes

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.R
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    plantillaBaseViewModel: PlantillaBaseViewModel, // ✅ recibido, no se crea aquí
    navController: NavController
) {
    val contexto = LocalContext.current
    val actividad = contexto as Activity

    // ✅ Callback que actualiza el fondo en el ViewModel compartido
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        plantillaBaseViewModel.actualizarFondo(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.inicializarIdioma(contexto)
    }

    PlantillaBase(
        viewModel = plantillaBaseViewModel, // ✅ usar el compartido
        mostrarBotonInfo = false,
        navController = navController
    ) { paddingValues ->

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
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.ajustes),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
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
                ) { navController.navigate("Perfil") }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Palette,
                    titulo = stringResource(R.string.color_interfaz),
                    descripcion = stringResource(R.string.personaliza_colores)
                ) { }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Wallpaper,
                    titulo = stringResource(R.string.cambiar_fondo),
                    descripcion = stringResource(R.string.personaliza_fondo)
                ) {
                    imageLauncher.launch("image/*")
                }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Wallpaper,
                    titulo = "Quitar fondo",
                    descripcion = "Volver al fondo por defecto"
                ) {
                    plantillaBaseViewModel.actualizarFondo(null)
                }
            }
            item {
                OpcionAjuste(
                    icono = Icons.Default.Info,
                    titulo = stringResource(R.string.version_app),
                    descripcion = stringResource(R.string.ver_version_actual)
                ) {
                    Toast.makeText(contexto, "Versión: 1.0", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpcionAjuste(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(descripcion, style = MaterialTheme.typography.bodySmall) },
            leadingContent = {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = VerdePrimario
                )
            }
        )
    }
}