package com.example.voxtask.ui.screens.Ajustes

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.ui.theme.VerdePrimario
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    navController: NavController
) {
    //Variables
    val viewModelPlantilla: PlantillaBaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val contexto = LocalContext.current
    val actividad = contexto as Activity

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController
    ) { paddingValues ->

        // Muestra las voces disponibles
        if (viewModel.mostrarSelectorVoz) {

            // Cargamos las voces
            LaunchedEffect(Unit) {
                viewModel.cargarVoces(contexto)
            }

            AlertDialog(
                onDismissRequest = { viewModel.mostrarSelectorVoz = false },
                title = { Text("Selecciona una voz") },
                text = {
                    // Si no hay voces mostramos un mensaje
                    if (viewModel.vocesDisponibles.isEmpty()) {
                        Text("No hay voces disponibles.")
                    } else {
                        // Lista scrolleable con todas las voces del movil
                        LazyColumn {
                            items(viewModel.vocesDisponibles) { voz ->
                                TextButton(
                                    onClick = { viewModel.aplicarVoz(voz.name, contexto) },
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
                    //Boton cancelar
                    TextButton(onClick = { viewModel.mostrarSelectorVoz = false }) {
                        Text("Cancelar")
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
                //Titulo de ajustes
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            //Boton de cambiar voz
            item {
                OpcionAjuste(
                    icono = Icons.Default.RecordVoiceOver,
                    titulo = "Cambiar voz",
                    descripcion = "Voz actual: ${viewModel.vozActual}"
                ) {
                    viewModel.mostrarSelectorVoz = true
                }
            }

            //Boton de cambiar idioma
            item {
                OpcionAjuste(
                    icono = Icons.Default.Language,
                    titulo = "Cambiar idioma",
                    descripcion = "Selecciona el idioma de la app"
                ) { }
            }

            //Boton mi perfil
            item {
                OpcionAjuste(
                    icono = Icons.Default.AccountCircle,
                    titulo = "Mi perfil",
                    descripcion = "Ver y editar tu información"
                ) {
                    navController.navigate("Perfil")
                }
            }
            //Boton cambiar color de la interfaz
            item {
                OpcionAjuste(
                    icono = Icons.Default.Palette,
                    titulo = "Color de la interfaz",
                    descripcion = "Personaliza los colores de la app"
                ) { }
            }
        }
    }
}
//Funcion que crea las opciones de los ajustes con un icono titulo y una descripcion
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