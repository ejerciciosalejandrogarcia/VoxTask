package com.example.voxtask.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioSesionScreen(
    navController: NavController,
){
    class LoginActivity : AppCompatActivity() {

        private lateinit var tilUsername: TextInputLayout
        private lateinit var tilPassword: TextInputLayout
        private lateinit var etUsername: TextInputEditText
        private lateinit var etPassword: TextInputEditText
        private lateinit var btnLogin: MaterialButton

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            tilUsername = findViewById(R.id.tilUsername)
            tilPassword = findViewById(R.id.tilPassword)
            etUsername = findViewById(R.id.etUsername)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)

            btnLogin.setOnClickListener {
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (validarCampos(username, password)) {
                    // Aquí va tu lógica de login (Firebase, API, etc.)
                    Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun validarCampos(username: String, password: String): Boolean {
            var esValido = true

            if (username.isEmpty()) {
                tilUsername.error = "Introduce tu nombre de usuario"
                esValido = false
            } else {
                tilUsername.error = null
            }

            if (password.isEmpty()) {
                tilPassword.error = "Introduce tu contraseña"
                esValido = false
            } else if (password.length < 6) {
                tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
                esValido = false
            } else {
                tilPassword.error = null
            }

            return esValido
        }
    }
}
