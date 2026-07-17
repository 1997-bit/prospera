package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.prospera.app.R
import com.prospera.app.adapters.HistorialPlanillaAdapter
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.HistorialPlanillaRow
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.helpers.GeneradorPdfReporte
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class ExpedienteColaboradorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMPLEADO_ID = "extra_empleado_id"
    }

    private lateinit var adapter: HistorialPlanillaAdapter
    private var empleadoActual: EmpleadoEntity? = null
    private var historialActual: List<HistorialPlanillaRow> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expediente_individual)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val empleadoId = intent.getLongExtra(EXTRA_EMPLEADO_ID, -1L)
        if (empleadoId == -1L) {
            Toast.makeText(this, "Colaborador no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = HistorialPlanillaAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvHistorial).apply {
            layoutManager = LinearLayoutManager(this@ExpedienteColaboradorActivity)
            adapter = this@ExpedienteColaboradorActivity.adapter
        }

        cargarExpediente(empleadoId)

        findViewById<MaterialButton>(R.id.btnImprimir).setOnClickListener { imprimirReporte() }
        findViewById<MaterialButton>(R.id.btnEnviarCorreo).setOnClickListener { enviarPorCorreo() }
    }

    private fun cargarExpediente(empleadoId: Long) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val empleado = db.empleadoDao().buscarPorId(empleadoId)
            if (empleado == null) {
                Toast.makeText(this@ExpedienteColaboradorActivity, "Colaborador no encontrado", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            empleadoActual = empleado
            historialActual = db.planillaDao().historialPorEmpleado(empleadoId)

            mostrarDatos(empleado)
            adapter.actualizarFilas(historialActual)

            findViewById<RecyclerView>(R.id.rvHistorial).visibility =
                if (historialActual.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            findViewById<TextView>(R.id.tvSinHistorial).visibility =
                if (historialActual.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun mostrarDatos(empleado: EmpleadoEntity) {
        findViewById<TextView>(R.id.tvNombre).text = empleado.nombre
        findViewById<TextView>(R.id.tvCargo).text = "${empleado.cargo} · ${empleado.departamento}"

        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        if (empleado.activo) {
            tvEstado.text = "ACTIVO"
            tvEstado.setBackgroundResource(R.drawable.bg_chip_activo)
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.md_on_primary_container))
        } else {
            tvEstado.text = "INACTIVO"
            tvEstado.setBackgroundResource(R.drawable.bg_chip_inactivo)
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.md_on_surface_variant))
        }

        setFilaDato(R.id.filaCedula, "Cédula", empleado.cedula)
        setFilaDato(R.id.filaDepartamento, "Departamento", empleado.departamento)
        setFilaDato(R.id.filaSalario, "Salario base", String.format(Locale("es", "PA"), "B/. %.2f", empleado.salarioBase))
        setFilaDato(R.id.filaAnioIngreso, "Año de ingreso", empleado.anioInicio.toString())
        setFilaDato(R.id.filaEstadoCivil, "Estado civil", empleado.estadoCivil.replaceFirstChar { it.uppercase() })
    }

    private fun setFilaDato(includeId: Int, etiqueta: String, valor: String) {
        val fila = findViewById<android.view.View>(includeId)
        fila.findViewById<TextView>(R.id.tvEtiqueta).text = etiqueta
        fila.findViewById<TextView>(R.id.tvValor).text = valor
    }

    private fun generarPdf(): File? {
        val empleado = empleadoActual ?: return null
        return GeneradorPdfReporte.generarExpedienteIndividual(this, empleado, historialActual)
    }

    private fun enviarPorCorreo() {
        val pdfFile = generarPdf() ?: return
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expediente - ${empleadoActual?.nombre}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar reporte"))
    }

    private fun imprimirReporte() {
        val pdfFile = generarPdf() ?: return
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val adapter = PdfPrintDocumentAdapter(pdfFile, "expediente")
        printManager.print("Expediente de colaborador", adapter, PrintAttributes.Builder().build())
    }

    private class PdfPrintDocumentAdapter(
        private val pdfFile: File,
        private val nombreDocumento: String
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(nombreDocumento)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build()
            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out android.print.PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            try {
                FileInputStream(pdfFile).use { input ->
                    FileOutputStream(destination?.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
            }
        }
    }
}