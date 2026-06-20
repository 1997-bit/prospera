package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.data.AuthRepository
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepo = AuthRepository(applicationContext)

        if (SessionManager.isLoggedIn(this)) {
            irAMain()
            return
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etUsuario)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvError = findViewById<TextView>(R.id.tvLoginError)
        val tvRegistrarme = findViewById<TextView>(R.id.tvRegistrarme)

        btnIngresar.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            tvError.visibility = android.view.View.GONE

            if (email.isBlank() || password.isBlank()) {
                tvError.text = getString(R.string.login_error_campos)
                tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = authRepo.login(email, password)
                if (usuario == null) {
                    tvError.text = getString(R.string.login_error_credenciales)
                    tvError.visibility = android.view.View.VISIBLE
                    return@launch
                }

                val empresas = authRepo.empresasDeUsuario(usuario.id)
                if (empresas.isEmpty()) {
                    tvError.text = getString(R.string.login_error_credenciales)
                    tvError.visibility = android.view.View.VISIBLE
                    return@launch
                }

                // Toma la primera empresa asociada (caso típico: una sola)
                SessionManager.login(this@LoginActivity, usuario.id, empresas.first().id)
                irAMain()
            }
        }

        tvRegistrarme.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}