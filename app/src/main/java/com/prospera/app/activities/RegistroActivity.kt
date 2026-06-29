package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.data.repository.AuthRepository
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        authRepo = AuthRepository(applicationContext)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etEmpresa = findViewById<TextInputEditText>(R.id.etEmpresa)
        val btnCrear = findViewById<Button>(R.id.btnCrearCuenta)
        val tvError = findViewById<TextView>(R.id.tvRegistroError)
        val tvYaTengo = findViewById<TextView>(R.id.tvYaTengoCuenta)

        btnCrear.setOnClickListener {
            val nombre = etNombre.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            val empresa = etEmpresa.text?.toString()?.trim().orEmpty()

            tvError.visibility = android.view.View.GONE

            if (nombre.isBlank() || email.isBlank() || password.isBlank() || empresa.isBlank()) {
                tvError.text = "Completa todos los campos"
                tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvError.text = "La contraseña debe tener al menos 6 caracteres"
                tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existente = authRepo.usuarioDao.buscarPorEmail(email)
                if (existente != null) {
                    tvError.text = "Ya existe una cuenta con ese correo"
                    tvError.visibility = android.view.View.VISIBLE
                    return@launch
                }

                val empresaId = authRepo.registrarPrimeraEmpresa(nombre, email, password, empresa)
                val usuario = authRepo.login(email, password)!!

                SessionManager.login(this@RegistroActivity, usuario.id, empresaId)
                startActivity(Intent(this@RegistroActivity, MainActivity::class.java))
                finishAffinity()
            }
        }

        tvYaTengo.setOnClickListener {
            finish()
        }
    }
}