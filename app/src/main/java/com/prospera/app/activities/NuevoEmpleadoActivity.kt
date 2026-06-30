package com.prospera.app.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.prospera.app.R
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.data.repository.EmpleadoRepository
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class NuevoEmpleadoActivity : AppCompatActivity() {

    private lateinit var repo: EmpleadoRepository
    private var empresaId: Long = -1
    private var empleadoId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_empleado)

        repo = EmpleadoRepository(applicationContext)
        empresaId = SessionManager.getEmpresaId(this)
        empleadoId = intent.getLongExtra("empleadoId", -1L).takeIf { it != -1L }

        configurarDropdownEstadoCivil()

        if (empleadoId != null) {
            cargarParaEditar(empleadoId!!)
            findViewById<Button>(R.id.btnEliminar).visibility = android.view.View.VISIBLE
        }

        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        findViewById<Button>(R.id.btnEliminar).setOnClickListener { confirmarEliminar() }
    }

    private fun configurarDropdownEstadoCivil() {
        val opciones = listOf("Soltero", "Casado", "Unido")
        findViewById<AutoCompleteTextView>(R.id.actvEstadoCivil).setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opciones)
        )
    }

    private fun cargarParaEditar(id: Long) {
        lifecycleScope.launch {
            val empleado = repo.obtener(id) ?: return@launch
            findViewById<TextInputEditText>(R.id.etNombre).setText(empleado.nombre)
            findViewById<TextInputEditText>(R.id.etCedula).setText(empleado.cedula)
            findViewById<TextInputEditText>(R.id.etCargo).setText(empleado.cargo)
            findViewById<TextInputEditText>(R.id.etDepartamento).setText(empleado.departamento)
            findViewById<TextInputEditText>(R.id.etSalario).setText(empleado.salarioBase.toString())
            findViewById<TextInputEditText>(R.id.etAnioIngreso).setText(empleado.anioInicio.toString())
            findViewById<AutoCompleteTextView>(R.id.actvEstadoCivil)
                .setText(empleado.estadoCivil.replaceFirstChar { it.uppercase() }, false)
        }
    }

    private fun guardar() {
        val nombre = findViewById<TextInputEditText>(R.id.etNombre).text?.toString()?.trim().orEmpty()
        val cedula = findViewById<TextInputEditText>(R.id.etCedula).text?.toString()?.trim().orEmpty()
        val cargo = findViewById<TextInputEditText>(R.id.etCargo).text?.toString()?.trim().orEmpty()
        val departamento = findViewById<TextInputEditText>(R.id.etDepartamento).text?.toString()?.trim().orEmpty()
        val salario = findViewById<TextInputEditText>(R.id.etSalario).text?.toString()?.toDoubleOrNull()
        val anioTexto = findViewById<TextInputEditText>(R.id.etAnioIngreso).text?.toString()?.trim().orEmpty()
        val estadoCivilTexto = findViewById<AutoCompleteTextView>(R.id.actvEstadoCivil).text?.toString().orEmpty()

        android.util.Log.e("PROSPERA", "INTENTO GUARDAR nombre=$nombre cedula=$cedula cargo=$cargo salario=$salario anio=$anioTexto empresaId=$empresaId")

        if (nombre.isBlank() || cedula.isBlank() || cargo.isBlank() || salario == null || anioTexto.isBlank()) {
            android.util.Log.d("PROSPERA", "VALIDACION FALLO, return")
            return
        }

        val anio = anioTexto.toIntOrNull()
        if (anio == null) {
            android.util.Log.d("PROSPERA", "ANIO INVALIDO: $anioTexto")
            return
        }

        val estadoCivil = estadoCivilTexto.lowercase().ifBlank { "soltero" }

        lifecycleScope.launch {
            try {
                android.util.Log.d("PROSPERA", "Verificando cedula disponible...")
                val disponible = repo.cedulaDisponible(cedula, empresaId, empleadoId)
                android.util.Log.d("PROSPERA", "cedula disponible = $disponible")

                if (!disponible) {
                    findViewById<TextInputEditText>(R.id.etCedula).error = "Ya existe un colaborador con esta cédula"
                    return@launch
                }

                android.util.Log.d("PROSPERA", "Guardando en Room...")
                val resultado = repo.guardar(
                    empresaId = empresaId,
                    nombre = nombre,
                    cedula = cedula,
                    cargo = cargo,
                    departamento = departamento,
                    estadoCivil = estadoCivil,
                    salarioBase = salario,
                    anioInicio = anio,
                    idExistente = empleadoId
                )
                android.util.Log.d("PROSPERA", "GUARDADO OK, id=$resultado")
                finish()
            } catch (e: Exception) {
                android.util.Log.e("PROSPERA", "ERROR AL GUARDAR", e)
            }
        }
    }

    private fun confirmarEliminar() {
        val id = empleadoId ?: return
        lifecycleScope.launch {
            repo.eliminar(id)
            finish()
        }
    }
}