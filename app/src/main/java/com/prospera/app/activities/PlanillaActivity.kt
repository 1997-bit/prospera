package com.prospera.app.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.prospera.app.R
import com.prospera.app.adapters.DetallePlanillaAdapter
import com.prospera.app.adapters.FilaPlanilla
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.data.repository.PreferenciasRepository
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

        binding.toolbar.setNavigationOnClickListener { finish() }

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

        adapter = DetallePlanillaAdapter(emptyList()) { detalleId, heDiurnas, heNocturnas, com, die, adelanto, ahorro ->
            lifecycleScope.launch {
                try {
                    repository.actualizarLinea(
                        detalleId, heDiurnas, heNocturnas, com, die, adelanto, ahorro
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

        cargarPeriodo()
    }

    private fun configurarSelectorPeriodo() {
        val actvMes = binding.actvMes
        actvMes.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, etiquetasMes))
        actvMes.setText(etiquetasMes[mes - 1], false)
        actvMes.setOnItemClickListener { _, _, position, _ ->
            mes = position + 1
            cargarPeriodo()
        }

        val anios = (anio - 2..anio + 1).toList()
        val actvAnio = binding.actvAnio
        actvAnio.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, anios.map { it.toString() }))
        actvAnio.setText(anio.toString(), false)
        actvAnio.setOnItemClickListener { _, _, position, _ ->
            anio = anios[position]
            cargarPeriodo()
        }

        val botonesQuincena = listOf(binding.btnQuincena1, binding.btnQuincena2)
        botonesQuincena[periodosDisponibles.indexOf(periodo)].isChecked = true
        binding.toggleQuincena.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val posicion = if (checkedId == binding.btnQuincena1.id) 0 else 1
            periodo = periodosDisponibles[posicion]
            cargarPeriodo()
        }
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
        binding.tvPeriodo.text = "${etiquetasQuincena[periodosDisponibles.indexOf(periodo)]} · ${etiquetasMes[mes - 1]} $anio"
        binding.btnCalcular.isEnabled = estado == "borrador"

        val pagada = estado == "pagada"
        binding.tvEstadoPlanilla.text = estado.uppercase()
        binding.tvEstadoPlanilla.setBackgroundResource(
            if (pagada) R.drawable.bg_chip_activo else R.drawable.bg_chip_inactivo
        )
        binding.tvEstadoPlanilla.setTextColor(
            ContextCompat.getColor(this, if (pagada) R.color.md_on_primary_container else R.color.md_on_surface_variant)
        )
    }

    private suspend fun cargarDetalles() {
        val db = AppDatabase.getInstance(applicationContext)
        val detalles = repository.detallesDePlanilla(planillaId)
        val filas = detalles.map { detalle ->
            val empleado = db.empleadoDao().buscarPorId(detalle.empleadoId)!!
            FilaPlanilla(detalle, empleado)
        }
        adapter.actualizarFilas(filas)
        pintarResumen(detalles)
    }

    private fun pintarResumen(detalles: List<DetallePlanillaEntity>) {
        binding.tvColaboradoresResumen.text =
            getString(R.string.planilla_colaboradores_conteo, detalles.size)
        binding.tvTotalBruto.text = Moneda.formatear(detalles.sumOf { it.salarioBruto })
        binding.tvTotalDescuentos.text = Moneda.formatear(detalles.sumOf { it.totalDescuentos })
        binding.tvTotalNeto.text = Moneda.formatear(detalles.sumOf { it.salarioNeto })
    }
}
