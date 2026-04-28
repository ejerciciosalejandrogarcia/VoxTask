package com.example.voxtask.ui.screens.Verificacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voxtask.ui.screens.Cambiar_contrasenia.CambiarContraseniaViewModel
import com.example.voxtask.ui.theme.*

@Composable
fun VerificacionScreen(
    viewModel: VerificacionViewModel = viewModel(),
    email: String = "",
    navController: NavController,
    onCodigoCompleto: (String) -> Unit = {}
) {
    var codigo by remember { mutableStateOf(List(6) { "" }) }

    val codigoUnido = codigo.joinToString("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBlanco)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Verifica tu correo electrónico",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = VerdePrimario
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hemos enviado un código a:",
            fontSize = 14.sp,
            color = TextoGris
        )

        Text(
            text = email,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = VerdePrimario
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 6 cajas del código
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                OutlinedTextField(
                    value = codigo[i],
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            val nuevo = codigo.toMutableList()
                            nuevo[i] = value
                            codigo = nuevo

                            // Auto avanzar lógica simple
                            if (value.isNotEmpty() && i < 5) {
                                // opcional: focus manager aquí
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerdePrimario,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = VerdePrimario
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (codigoUnido.length == 6) {
                    onCodigoCompleto(codigoUnido)
                }
            },
            enabled = codigoUnido.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrimario,
                disabledContainerColor = Color.Gray
            )
        ) {
            Text(
                text = "Verificar código",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}