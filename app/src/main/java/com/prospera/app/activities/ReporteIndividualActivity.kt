package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.adapters.ColaboradorSeleccionableAdapter
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class ReporteIndividualActivity : AppCompatActivity() {

    private lateinit var adapter: ColaboradorSeleccionableAdapter
    private var empresaId: Long = 0
    private var todosLosColaboradores: List<EmpleadoEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_individual)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        empresaId = SessionManager.getEmpresaId(this)

        adapter = ColaboradorSeleccionableAdapter(emptyList()) { colaborador ->
            val intent = Intent(this, ExpedienteColaboradorActivity::class.java)
            intent.putExtra(ExpedienteColaboradorActivity.EXTRA_EMPLEADO_ID, colaborador.id)
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.rvColaboradores).apply {
            layoutManager = LinearLayoutManager(this@ReporteIndividualActivity)
            adapter = this@ReporteIndividualActivity.adapter
        }

        cargarColaboradores()
        configurarBusqueda()
    }

    private fun cargarColaboradores() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            todosLosColaboradores = db.empleadoDao().listarTodos(empresaId)
            mostrarLista(todosLosColaboradores)
        }
    }

    private fun configurarBusqueda() {
        findViewById<TextInputEditText>(R.id.etBuscar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                val filtrados = if (query.isBlank()) {
                    todosLosColaboradores
                } else {
                    todosLosColaboradores.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                                it.cedula.contains(query, ignoreCase = true) ||
                                it.cargo.contains(query, ignoreCase = true)
                    }
                }
                mostrarLista(filtrados)
            }
        })
    }

    private fun mostrarLista(lista: List<EmpleadoEntity>) {
        adapter.actualizarLista(lista)
        findViewById<RecyclerView>(R.id.rvColaboradores).visibility =
            if (lista.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        findViewById<android.widget.TextView>(R.id.tvSinResultados).visibility =
            if (lista.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}