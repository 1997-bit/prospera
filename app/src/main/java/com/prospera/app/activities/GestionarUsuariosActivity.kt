package com.prospera.app.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.adapters.UsuariosAdapter
import com.prospera.app.data.repository.AuthRepository
import com.prospera.app.data.UsuarioConRol
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class GestionarUsuariosActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository
    private var empresaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_usuarios)

        authRepo = AuthRepository(applicationContext)
        empresaId = SessionManager.getEmpresaId(this)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val rolDropdown = findViewById<AutoCompleteTextView>(R.id.actvRol)
        rolDropdown.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listOf(
                    getString(R.string.usuarios_rol_admin),
                    getString(R.string.usuarios_rol_contador),
                    getString(R.string.usuarios_rol_visor)
                )
            )
        )

        cargarUsuarios()

        findViewById<Button>(R.id.btnCrearUsuario).setOnClickListener {
            crearUsuario(rolDropdown)
        }
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch {
            val lista = authRepo.usuariosDeEmpresa(empresaId)
            pintarLista(lista)
        }
    }

    private fun pintarLista(lista: List<UsuarioConRol>) {
        val rv = findViewById<RecyclerView>(R.id.rvUsuarios)
        val tvVacio = findViewById<TextView>(R.id.tvUsuariosVacio)

        if (lista.size <= 1) {
            rv.visibility = android.view.View.GONE
            tvVacio.visibility = android.view.View.VISIBLE
        } else {
            rv.visibility = android.view.View.VISIBLE
            tvVacio.visibility = android.view.View.GONE
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = UsuariosAdapter(lista)
        }
    }

    private fun crearUsuario(rolDropdown: AutoCompleteTextView) {
        val nombre = findViewById<TextInputEditText>(R.id.etNombreNuevo).text?.toString()?.trim().orEmpty()
        val email = findViewById<TextInputEditText>(R.id.etEmailNuevo).text?.toString()?.trim().orEmpty()
        val password = findViewById<TextInputEditText>(R.id.etPasswordNuevo).text?.toString().orEmpty()
        val rolTexto = rolDropdown.text?.toString().orEmpty()

        if (nombre.isBlank() || email.isBlank() || password.isBlank() || rolTexto.isBlank()) {
            return
        }

        val rol = when (rolTexto) {
            getString(R.string.usuarios_rol_admin) -> "admin"
            getString(R.string.usuarios_rol_contador) -> "contador"
            else -> "visor"
        }

        lifecycleScope.launch {
            authRepo.crearUsuarioParaEmpresa(empresaId, nombre, email, password, rol)
            cargarUsuarios()
            findViewById<TextInputEditText>(R.id.etNombreNuevo).text?.clear()
            findViewById<TextInputEditText>(R.id.etEmailNuevo).text?.clear()
            findViewById<TextInputEditText>(R.id.etPasswordNuevo).text?.clear()
            rolDropdown.text?.clear()
        }
    }
}