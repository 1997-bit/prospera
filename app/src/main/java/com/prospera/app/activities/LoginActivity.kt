package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Ya logueado va directo a Main, no repetir login
        if (SessionManager.isLoggedIn(this)) {
            irAMain()
            return
        }

        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val btnIngresar = findViewById<android.widget.Button>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            // Passthrough: no valida nada, solo guarda sesión y avanza
            val usuario = etUsuario.text?.toString()?.trim().orEmpty()
            SessionManager.login(this, usuario)
            irAMain()
        }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}