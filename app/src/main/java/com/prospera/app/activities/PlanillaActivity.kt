package com.prospera.app.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.prospera.app.R
import com.prospera.app.adapters.DetallePlanillaAdapter
import com.prospera.app.adapters.FilaPlanilla
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.databinding.ActivityPlanillaBinding
import com.prospera.app.utils.Moneda
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
    private var filasActuales: List<FilaPlanilla> = emptyList()

    private val opcionesPeriodo = listOf(
        "1ra quincena" to "1ra_quincena",
        "2da quincena" to "2da_quincena"
    )
    private val opcionesMes = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

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
        configurarSelectorPeriodo(cal.get(Calendar.YEAR))

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
        configurarTabs()

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

        abrirPlanillaSeleccionada()
    }

    override fun onResume() {
        super.onResume()
        if (planillaId != 0L) abrirPlanillaSeleccionada()
    }

    private fun configurarSelectorPeriodo(anioActual: Int) {
        binding.actvPeriodo.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesPeriodo.map { it.first })
        )
        binding.actvMes.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesMes)
        )
        binding.actvAnio.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, (anioActual - 2..anioActual + 1).map { it.toString() })
        )

        binding.actvPeriodo.setText(etiquetaPeriodo(periodo), false)
        binding.actvMes.setText(opcionesMes[mes - 1], false)
        binding.actvAnio.setText(anio.toString(), false)
        binding.btnAbrirPeriodo.setOnClickListener { abrirPlanillaSeleccionada() }
    }

    private fun abrirPlanillaSeleccionada() {
        actualizarPeriodoDesdeSelector()
        lifecycleScope.launch {
            val planilla = repository.generarOAbrir(empresaId, periodo, mes, anio)
            planillaId = planilla.id
            actualizarEncabezado(planilla.estado)
            cargarDetalles()
        }
    }

    private fun actualizarPeriodoDesdeSelector() {
        val periodoTexto = binding.actvPeriodo.text?.toString().orEmpty()
        periodo = opcionesPeriodo.firstOrNull { it.first == periodoTexto }?.second ?: periodo

        val mesTexto = binding.actvMes.text?.toString().orEmpty()
        mes = opcionesMes.indexOf(mesTexto).takeIf { it >= 0 }?.plus(1) ?: mes

        anio = binding.actvAnio.text?.toString()?.toIntOrNull() ?: anio
    }

    private fun etiquetaPeriodo(periodo: String): String =
        opcionesPeriodo.firstOrNull { it.second == periodo }?.first ?: periodo

    private fun configurarTabs() {
        binding.tabPlanilla.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = mostrarTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = mostrarTab(tab.position)
        })
        mostrarTab(0)
    }

    private fun mostrarTab(posicion: Int) {
        val esCalculo = posicion == 0
        binding.rvPlanilla.visibility = if (esCalculo) View.VISIBLE else View.GONE
        binding.btnCalcular.visibility = if (esCalculo) View.VISIBLE else View.GONE
        binding.cardContenidoTab.visibility = if (esCalculo) View.GONE else View.VISIBLE

        binding.tvContenidoTab.text = when (posicion) {
            1 -> textoHistorial()
            2 -> textoResumen()
            else -> ""
        }
    }

    private fun textoHistorial(): String =
        "Historial de planilla\n\n" +
            "Aquí se consultan las planillas generadas y pagadas por período.\n\n" +
            "Para ver reportes consolidados, entra al módulo Reportes."

    private fun textoResumen(): String {
        val totalBruto = filasActuales.sumOf { it.detalle.salarioBruto }
        val totalDescuentos = filasActuales.sumOf { it.detalle.totalDescuentos }
        val totalNeto = filasActuales.sumOf { it.detalle.salarioNeto }

        return "Resumen de la planilla\n\n" +
            "Colaboradores: ${filasActuales.size}\n" +
            "Total bruto: ${Moneda.formatear(totalBruto)}\n" +
            "Total descuentos: ${Moneda.formatear(totalDescuentos)}\n" +
            "Total neto: ${Moneda.formatear(totalNeto)}"
    }

    private fun actualizarEncabezado(estado: String) {
        binding.tvEstadoPlanilla.text = "${etiquetaPeriodo(periodo)} · ${opcionesMes[mes - 1]} $anio · ${estado.uppercase()}"
        binding.btnCalcular.isEnabled = estado == "borrador"
    }

    private suspend fun cargarDetalles() {
        val db = AppDatabase.getInstance(applicationContext)
        val detalles = repository.detallesDePlanilla(planillaId)
        val filas = detalles.map { detalle ->
            val empleado = db.empleadoDao().buscarPorId(detalle.empleadoId)!!
            FilaPlanilla(detalle, empleado)
        }
        filasActuales = filas
        adapter.actualizarFilas(filas)
        mostrarTab(binding.tabPlanilla.selectedTabPosition)
    }
}
