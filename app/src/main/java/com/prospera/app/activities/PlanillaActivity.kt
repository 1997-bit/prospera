package com.prospera.app.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.prospera.app.R
import com.prospera.app.adapters.DetallePlanillaAdapter
import com.prospera.app.adapters.FilaPlanilla
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.databinding.ActivityPlanillaBinding
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class PlanillaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanillaBinding
    private lateinit var repository: PlanillaRepository
    private lateinit var adapter: DetallePlanillaAdapter

    private var planillaId: Long = 0
    private var empresaId: Long = 0

    // TODO: reemplazar por selector real de periodo/mes/año en UI;
    // por ahora toma la quincena actual del sistema para probar el flujo.
    private var periodo = "1ra_quincena"
    private var mes = 0
    private var anio = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanillaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(applicationContext)
        repository = PlanillaRepository(
            planillaDao = db.planillaDao(),
            empleadoDao = db.empleadoDao(),
            empresaDao = db.empresaDao()
        )

        empresaId = SessionManager.getEmpresaId(this)

        val cal = Calendar.getInstance()
        mes = cal.get(Calendar.MONTH) + 1
        anio = cal.get(Calendar.YEAR)
        periodo = if (cal.get(Calendar.DAY_OF_MONTH) <= 15) "1ra_quincena" else "2da_quincena"

        adapter = DetallePlanillaAdapter(emptyList()) { detalleId, he, com, die, pri, otros ->
            lifecycleScope.launch {
                try {
                    repository.actualizarLinea(detalleId, he, com, die, pri, otros)
                    cargarDetalles()
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@PlanillaActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.rvPlanilla.layoutManager = LinearLayoutManager(this)
        binding.rvPlanilla.adapter = adapter

        binding.btnCalcular.setOnClickListener {
            lifecycleScope.launch {
                try {
                    repository.marcarPagada(planillaId)
                    actualizarEncabezado(estado = "pagada")
                    Toast.makeText(this@PlanillaActivity, "Planilla marcada como pagada", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@PlanillaActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            val planilla = repository.generarOAbrir(empresaId, periodo, mes, anio)
            planillaId = planilla.id
            actualizarEncabezado(planilla.estado)
            cargarDetalles()
        }
    }

    private fun actualizarEncabezado(estado: String) {
        binding.tvEstadoPlanilla.text = "$periodo · $mes/$anio · ${estado.uppercase()}"
        binding.btnCalcular.isEnabled = estado == "borrador"
    }

    private suspend fun cargarDetalles() {
        val db = AppDatabase.getInstance(applicationContext)
        val detalles = repository.detallesDePlanilla(planillaId)
        val filas = detalles.map { detalle ->
            val empleado = db.empleadoDao().buscarPorId(detalle.empleadoId)!!
            FilaPlanilla(detalle, empleado)
        }
        adapter.actualizarFilas(filas)
    }
}