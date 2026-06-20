package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.adapters.EmpleadosAdapter
import com.prospera.app.data.EmpleadoEntity
import com.prospera.app.data.EmpleadoRepository
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class EmpleadosActivity : AppCompatActivity() {

    private lateinit var repo: EmpleadoRepository
    private lateinit var adapter: EmpleadosAdapter
    private var empresaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empleados)

        repo = EmpleadoRepository(applicationContext)
        empresaId = SessionManager.getEmpresaId(this)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        adapter = EmpleadosAdapter(emptyList()) { empleado ->
            abrirEdicion(empleado)
        }
        findViewById<RecyclerView>(R.id.rvEmpleados).apply {
            layoutManager = LinearLayoutManager(this@EmpleadosActivity)
            adapter = this@EmpleadosActivity.adapter
        }

        configurarBuscador()
        cargarEmpleados()

        findViewById<android.view.View>(R.id.fabAgregar).setOnClickListener {
            startActivity(Intent(this, NuevoEmpleadoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEmpleados() // refresca al volver de crear/editar
    }

    private fun configurarBuscador() {
        findViewById<TextInputEditText>(R.id.etBuscar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                buscar(s?.toString().orEmpty())
            }
        })
    }

    private fun cargarEmpleados() {
        lifecycleScope.launch {
            val lista = repo.listarActivos(empresaId)
            pintarLista(lista)
        }
    }

    private fun buscar(query: String) {
        lifecycleScope.launch {
            val lista = repo.buscar(empresaId, query)
            pintarLista(lista)
        }
    }

    private fun pintarLista(lista: List<EmpleadoEntity>) {
        val rv = findViewById<RecyclerView>(R.id.rvEmpleados)
        val tvVacio = findViewById<TextView>(R.id.tvVacio)

        if (lista.isEmpty()) {
            rv.visibility = android.view.View.GONE
            tvVacio.visibility = android.view.View.VISIBLE
        } else {
            rv.visibility = android.view.View.VISIBLE
            tvVacio.visibility = android.view.View.GONE
            adapter.actualizar(lista)
        }
    }

    private fun abrirEdicion(empleado: EmpleadoEntity) {
        startActivity(
            Intent(this, NuevoEmpleadoActivity::class.java)
                .putExtra("empleadoId", empleado.id)
        )
    }
}