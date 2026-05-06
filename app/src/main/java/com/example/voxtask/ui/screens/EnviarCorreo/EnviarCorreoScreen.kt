// EnviarCorreoScreen.kt
package com.example.voxtask.ui.screens.EnviarCorreo

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voxtask.utils.PlantillaBase
import com.example.voxtask.utils.PlantillaBaseViewModel
import com.example.voxtask.utils.TextoAVoz
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun EnviarCorreoScreen(
    viewModelPlantilla: PlantillaBaseViewModel,
    viewModel: EnviarCorreoViewModel,
    navController: NavController
) {
    val contexto = LocalContext.current

    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
                    .getResult(ApiException::class.java)
                viewModel.guardarToken(contexto)
            } catch (e: ApiException) { }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar(contexto)
        TextoAVoz.hablar(contexto, "¿A quién se lo quieres enviar?")
    }

    PlantillaBase(
        viewModel = viewModelPlantilla,
        navController = navController,
        onTextoReconocido = { texto -> viewModel.procesarVoz(texto, contexto) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (viewModel.paso != PasoEnvio.ENVIANDO &&
                viewModel.paso != PasoEnvio.ENVIADO &&
                viewModel.paso != PasoEnvio.ERROR &&
                viewModel.paso != PasoEnvio.CONFIRMACION
            ) {
                Text(
                    text = "Paso ${viewModel.paso.ordinal + 1} de 4",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (viewModel.paso) {
                    PasoEnvio.DESTINATARIO -> PasoUI(
                        titulo = "¿A quién envías el correo?",
                        descripcion = "Di el correo electrónico del destinatario",
                        valor = viewModel.destinatario
                    )
                    PasoEnvio.ASUNTO -> PasoUI(
                        titulo = "¿Cuál es el asunto?",
                        descripcion = "Di el asunto del correo",
                        valor = viewModel.asunto
                    )
                    PasoEnvio.MODO -> PasoUI(
                        titulo = "¿Cómo quieres el mensaje?",
                        descripcion = "Di \"yo mismo\" para escribirlo tú\nDi \"inteligencia artificial\" para que lo cree la IA",
                        valor = ""
                    )
                    PasoEnvio.MENSAJE -> PasoUI(
                        titulo = if (viewModel.modo == "ia") "¿Sobre qué trata el correo?" else "Dicta el mensaje",
                        descripcion = if (viewModel.modo == "ia") "Di el tema o idea principal" else "Di el contenido completo del correo",
                        valor = viewModel.mensaje
                    )
                    PasoEnvio.CONFIRMACION -> ConfirmacionUI(
                        destinatario = viewModel.destinatario,
                        asunto = viewModel.asunto,
                        mensaje = viewModel.mensaje,
                        modo = viewModel.modo,
                        onConfirmar = { viewModel.confirmarEnvio(contexto) },
                        onEditar = { campo -> viewModel.editarCampo(campo) }
                    )
                    PasoEnvio.ENVIANDO -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Enviando correo...", color = MaterialTheme.colorScheme.primary)
                    }
                    PasoEnvio.ENVIADO -> {
                        Text(
                            text = "✅ Correo enviado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        ResumenCorreo(
                            para = viewModel.destinatario,
                            asunto = viewModel.asunto,
                            modo = viewModel.modo
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.reiniciar(contexto) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Enviar otro", color = Color.White)
                        }
                    }
                    PasoEnvio.ERROR -> {
                        Text(
                            text = viewModel.errorMensaje,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.reiniciar(contexto) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Intentar de nuevo", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmacionUI(
    destinatario: String,
    asunto: String,
    mensaje: String,
    modo: String,
    onConfirmar: () -> Unit,
    onEditar: (String) -> Unit
) {
    // Estado local para saber qué campo está en modo edición
    var campoEditando by remember { mutableStateOf<String?>(null) }
    // Copias locales editables
    var destinatarioEdit by remember { mutableStateOf(destinatario) }
    var asuntoEdit by remember { mutableStateOf(asunto) }
    var mensajeEdit by remember { mutableStateOf(mensaje) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = "¿Así lo quieres enviar?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Revisa los datos antes de enviar",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilaConfirmacion(
                    etiqueta = "Para",
                    valor = destinatarioEdit,
                    editando = campoEditando == "destinatario",
                    onEditar = { campoEditando = "destinatario" },
                    onGuardar = { nuevoValor ->
                        destinatarioEdit = nuevoValor
                        campoEditando = null
                        onEditar("destinatario:$nuevoValor")
                    }
                )
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                FilaConfirmacion(
                    etiqueta = "Asunto",
                    valor = asuntoEdit,
                    editando = campoEditando == "asunto",
                    onEditar = { campoEditando = "asunto" },
                    onGuardar = { nuevoValor ->
                        asuntoEdit = nuevoValor
                        campoEditando = null
                        onEditar("asunto:$nuevoValor")
                    }
                )
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                FilaConfirmacion(
                    etiqueta = "Mensaje",
                    valor = mensajeEdit,
                    editando = campoEditando == "mensaje",
                    onEditar = { campoEditando = "mensaje" },
                    onGuardar = { nuevoValor ->
                        mensajeEdit = nuevoValor
                        campoEditando = null
                        onEditar("mensaje:$nuevoValor")
                    }
                )
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                FilaConfirmacion(
                    etiqueta = "Modo",
                    valor = if (modo == "ia") "Generado por IA" else "Manual",
                    editando = false,      // el modo no se edita manualmente
                    onEditar = { },
                    onGuardar = { }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onConfirmar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Enviar correo",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FilaConfirmacion(
    etiqueta: String,
    valor: String,
    editando: Boolean,
    onEditar: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var textoTemporal by remember(valor) { mutableStateOf(valor) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (editando) {
                OutlinedTextField(
                    value = textoTemporal,
                    onValueChange = { textoTemporal = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = etiqueta != "Mensaje",
                    minLines = if (etiqueta == "Mensaje") 3 else 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = valor.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        // Botón cambia entre "Editar" y "Guardar" según el estado
        if (editando) {
            Button(
                onClick = { onGuardar(textoTemporal) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        } else {
            OutlinedButton(
                onClick = onEditar,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Editar", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PasoUI(titulo: String, descripcion: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (valor.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = valor,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ResumenCorreo(para: String, asunto: String, modo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Para: $para", fontWeight = FontWeight.Medium)
            Text("Asunto: $asunto", fontWeight = FontWeight.Medium)
            Text(
                "Modo: ${if (modo == "ia") "Generado por IA" else "Manual"}",
                fontWeight = FontWeight.Medium
            )
        }
    }
}