package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prospera.app.R
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.repository.AuthRepository
import com.prospera.app.data.repository.EmpleadoRepository
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.data.repository.PreferenciasRepository
import com.prospera.app.data.ResumenMensual
import com.prospera.app.utils.Moneda
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository
    private lateinit var empleadoRepo: EmpleadoRepository
    private lateinit var planillaRepo: PlanillaRepository
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
        val db = AppDatabase.getInstance(applicationContext)
        planillaRepo = PlanillaRepository(
            planillaDao = db.planillaDao(),
            empleadoDao = db.empleadoDao(),
            empresaDao = db.empresaDao(),
            preferenciasRepository = PreferenciasRepository(applicationContext)
        )
        empresaId = SessionManager.getEmpresaId(this)

        pintarHeader()
        cargarResumen()
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
            val cal = Calendar.getInstance()
            val mes = cal.get(Calendar.MONTH) + 1
            val anio = cal.get(Calendar.YEAR)

            val fila = planillaRepo.resumenMensual(empresaId, mes, anio)
            val totalActivos = empleadoRepo.listarActivos(empresaId).size

            val resumen = ResumenMensual.vacio().copy(
                totalBruto = fila.totalBruto,
                totalDescuentos = fila.totalDescuentos,
                totalNeto = fila.totalNeto,
                colaboradoresActivos = totalActivos
            )
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