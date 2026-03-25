package com.example.voxtask.ui.screens.Inicio


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxtask.utils.TextoAVoz
import kotlinx.coroutines.launch

class InicioViewModel : ViewModel() {

    // Variables para guardar el texto reconocido
    var textoReconocido: String = ""
        private set

    // Estado para la UI
    var estaEscuchando = mutableStateOf(false)
        private set

    var textoMostrado = mutableStateOf("")
        private set

    fun procesarTextoReconocido(texto: String?, contexto: android.content.Context) {
        estaEscuchando.value = false

        if (!texto.isNullOrEmpty()) {
            // Guardar en la variable
            textoReconocido = texto
            textoMostrado.value = texto

            // Procesar el comando
            procesarComandoVoz(texto, contexto)
        } else {
            textoMostrado.value = ""
            viewModelScope.launch {
                TextoAVoz.hablar(contexto, "No te entendí, por favor intenta de nuevo")
            }
        }
    }

    fun iniciarEscucha() {
        estaEscuchando.value = true
    }

    fun limpiarTexto() {
        textoReconocido = ""
        textoMostrado.value = ""
    }

    private fun procesarComandoVoz(comando: String, contexto: android.content.Context) {
        val comandoConvertido = comando.lowercase()

        viewModelScope.launch {
            when {
                comandoConvertido.contains("contador") || comandoConvertido.contains("cuenta") -> {
                    TextoAVoz.hablar(contexto, "Abriendo el contador")
                    // Aquí vamos al contador
                }
                comandoConvertido.contains("correo") || comandoConvertido.contains("email") -> {
                    TextoAVoz.hablar(contexto, "Abriendo el correo")
                    // Aquí vamos al correo
                }
                comandoConvertido.contains("alarma") || comandoConvertido.contains("alarma") -> {
                    TextoAVoz.hablar(contexto, "Abriendo las alarmas")
                    // Aquí vamos a alarmas
                }
                comandoConvertido.contains("lista") || comandoConvertido.contains("compra") -> {
                    TextoAVoz.hablar(contexto, "Abriendo la lista de compras")
                    // Aquí vamos a lista de compras
                }
                else -> {
                    TextoAVoz.hablar(
                        contexto,
                        "No entendí ese comando, por favor elige una de las opciones: contador, correo, alarma o lista de compras"
                    )
                }
            }
        }
    }
}
