package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.adapters.ActividadAdapter
import com.prospera.app.data.ActividadReciente
import com.prospera.app.data.repository.AuthRepository
import com.prospera.app.data.repository.EmpleadoRepository
import com.prospera.app.data.ResumenMensual
import com.prospera.app.utils.Moneda
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository
    private lateinit var empleadoRepo: EmpleadoRepository
    private var empresaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        authRepo = AuthRepository(applicationContext)
        empleadoRepo = EmpleadoRepository(applicationContext)
        empresaId = SessionManager.getEmpresaId(this)

        pintarHeader()
        cargarResumen()
        pintarActividad(obtenerActividadReciente())
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()
        cargarResumen() // refresca al volver de Empleados (se creó/borró alguien)
    }

    private fun pintarHeader() {
        lifecycleScope.launch {
            val usuarioId = SessionManager.getUsuarioId(this@MainActivity)
            val usuario = authRepo.usuarioDao.buscarPorId(usuarioId)
            val nombre = usuario?.nombre ?: "Administrador"
            findViewById<TextView>(R.id.tvAvatarIniciales).text = iniciales(nombre)
        }
    }

    private fun iniciales(nombre: String): String =
        nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "AD" }

    private fun cargarResumen() {
        lifecycleScope.launch {
            val base = ResumenMensual.vacio()
            val total = empleadoRepo.listarActivos(empresaId).size
            val resumen = base.copy(colaboradoresActivos = total)
            pintarResumen(resumen)
        }
    }

    private fun pintarResumen(resumen: ResumenMensual) {
        findViewById<TextView>(R.id.tvPeriodo).text =
            "${getString(R.string.main_resumen_periodo_prefijo)} ${resumen.mesAnio}"

        findViewById<TextView>(R.id.tvNetoTotal).text = Moneda.formatear(resumen.totalNeto)
        findViewById<TextView>(R.id.tvTotalDescuentos).text = Moneda.formatear(resumen.totalDescuentos)
        findViewById<TextView>(R.id.tvNetoAPagar).text = Moneda.formatear(resumen.totalNeto)

        findViewById<TextView>(R.id.tvColaboradoresActivos).text =
            getString(R.string.main_resumen_colaboradores, resumen.colaboradoresActivos)
    }

    /** TODO: reemplazar por consulta real a Room (historial) cuando exista. */
    private fun obtenerActividadReciente(): List<ActividadReciente> = emptyList()

    private fun pintarActividad(items: List<ActividadReciente>) {
        val rv = findViewById<RecyclerView>(R.id.rvActividad)
        val tvVacio = findViewById<TextView>(R.id.tvActividadVacio)

        if (items.isEmpty()) {
            rv.visibility = android.view.View.GONE
            tvVacio.visibility = android.view.View.VISIBLE
        } else {
            rv.visibility = android.view.View.VISIBLE
            tvVacio.visibility = android.view.View.GONE
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = ActividadAdapter(items)
        }
    }

    private fun configurarNavegacion() {
        findViewById<android.view.View>(R.id.cardPersonal).setOnClickListener {
            startActivity(Intent(this, EmpleadosActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardPlanilla).setOnClickListener {
            startActivity(Intent(this, PlanillaActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardReportes).setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }
        findViewById<android.view.View>(R.id.avatarContainer).setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
    }
}