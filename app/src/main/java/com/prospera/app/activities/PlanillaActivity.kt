package com.prospera.app.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.prospera.app.R
import com.prospera.app.adapters.DetallePlanillaAdapter
import com.prospera.app.adapters.FilaPlanilla
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.data.repository.PreferenciasRepository
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

    private val periodosDisponibles = listOf("1ra_quincena", "2da_quincena")
    private val etiquetasQuincena = listOf("1ra Quincena", "2da Quincena")
    private val etiquetasMes = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    private var periodo = periodosDisponibles[0]
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
            empresaDao = db.empresaDao(),
            preferenciasRepository = PreferenciasRepository(applicationContext)
        )

        empresaId = SessionManager.getEmpresaId(this)

        val cal = Calendar.getInstance()
        mes = cal.get(Calendar.MONTH) + 1
        anio = cal.get(Calendar.YEAR)
        periodo = if (cal.get(Calendar.DAY_OF_MONTH) <= 15) periodosDisponibles[0] else periodosDisponibles[1]

        configurarSelectorPeriodo()

        adapter = DetallePlanillaAdapter(emptyList()) { detalleId, heDiurnas, heNocturnas, com, die, pri, muebleria, adelanto, ahorro ->
            lifecycleScope.launch {
                try {
                    repository.actualizarLinea(
                        detalleId, heDiurnas, heNocturnas, com, die, pri, muebleria, adelanto, ahorro
                    )
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

        binding.btnCargarPeriodo.setOnClickListener {
            leerSeleccionPeriodo()
            cargarPeriodo()
        }

        cargarPeriodo()
    }

    private fun configurarSelectorPeriodo() {
        binding.spMes.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etiquetasMes)
        binding.spMes.setSelection(mes - 1)

        val anios = (anio - 2..anio + 1).toList()
        binding.spAnio.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, anios)
        binding.spAnio.setSelection(anios.indexOf(anio))

        binding.spQuincena.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etiquetasQuincena)
        binding.spQuincena.setSelection(periodosDisponibles.indexOf(periodo))
    }

    private fun leerSeleccionPeriodo() {
        mes = binding.spMes.selectedItemPosition + 1
        anio = binding.spAnio.selectedItem as Int
        periodo = periodosDisponibles[binding.spQuincena.selectedItemPosition]
    }

    private fun cargarPeriodo() {
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