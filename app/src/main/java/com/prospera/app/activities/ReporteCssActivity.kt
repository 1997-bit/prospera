package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.prospera.app.R
import com.prospera.app.adapters.AporteCssAdapter
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.AporteCssRow
import com.prospera.app.data.repository.PlanillaRepository
import com.prospera.app.helpers.GeneradorPdfReporte
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class ReporteCssActivity : AppCompatActivity() {

    private lateinit var repository: PlanillaRepository
    private lateinit var adapter: AporteCssAdapter
    private var empresaId: Long = 0

    private var mesSeleccionado: Int = 0
    private var anioSeleccionado: Int = 0

    private var filasActuales: List<AporteCssRow> = emptyList()
    private var totalCss = 0.0

    private val nombresMeses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_css)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val db = AppDatabase.getInstance(applicationContext)
        repository = PlanillaRepository(
            planillaDao = db.planillaDao(),
            empleadoDao = db.empleadoDao(),
            empresaDao = db.empresaDao()
        )
        empresaId = SessionManager.getEmpresaId(this)

        adapter = AporteCssAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvAportesCss).apply {
            layoutManager = LinearLayoutManager(this@ReporteCssActivity)
            adapter = this@ReporteCssActivity.adapter
        }

        val cal = Calendar.getInstance()
        mesSeleccionado = cal.get(Calendar.MONTH) + 1
        anioSeleccionado = cal.get(Calendar.YEAR)

        configurarSelectores()
        cargarReporte()

        findViewById<MaterialButton>(R.id.btnImprimir).setOnClickListener { imprimirReporte() }
        findViewById<MaterialButton>(R.id.btnEnviarCorreo).setOnClickListener { enviarPorCorreo() }
    }

    private fun configurarSelectores() {
        val actvMes = findViewById<AutoCompleteTextView>(R.id.actvMes)
        actvMes.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, nombresMeses))
        actvMes.setText(nombresMeses[mesSeleccionado - 1], false)
        actvMes.setOnItemClickListener { _, _, position, _ ->
            mesSeleccionado = position + 1
            cargarReporte()
        }

        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        val anios = (anioActual - 4..anioActual).map { it.toString() }.reversed()
        val actvAnio = findViewById<AutoCompleteTextView>(R.id.actvAnio)
        actvAnio.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, anios))
        actvAnio.setText(anioSeleccionado.toString(), false)
        actvAnio.setOnItemClickListener { _, _, position, _ ->
            anioSeleccionado = anios[position].toInt()
            cargarReporte()
        }
    }

    private fun cargarReporte() {
        lifecycleScope.launch {
            filasActuales = repository.reporteCssMensual(empresaId, mesSeleccionado, anioSeleccionado)
            adapter.actualizarFilas(filasActuales)

            totalCss = filasActuales.sumOf { it.totalCss }
            actualizarResumen()

            findViewById<RecyclerView>(R.id.rvAportesCss).visibility =
                if (filasActuales.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            findViewById<TextView>(R.id.tvSinDatos).visibility =
                if (filasActuales.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun actualizarResumen() {
        findViewById<TextView>(R.id.tvPeriodoResumen).text = periodoTextoActual()
        findViewById<TextView>(R.id.tvColaboradoresResumen).text = "${filasActuales.size} colaboradores"
        findViewById<TextView>(R.id.tvTotalCss).text = formatoMoneda(totalCss)
    }

    private fun periodoTextoActual(): String = "${nombresMeses[mesSeleccionado - 1]} $anioSeleccionado"

    private fun generarPdf(): File? {
        if (filasActuales.isEmpty()) {
            Toast.makeText(this, "No hay datos para generar el reporte", Toast.LENGTH_SHORT).show()
            return null
        }
        return GeneradorPdfReporte.generarReporteCss(this, periodoTextoActual(), filasActuales, totalCss)
    }

    private fun enviarPorCorreo() {
        val pdfFile = generarPdf() ?: return
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte CSS - ${periodoTextoActual()}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar reporte"))
    }

    private fun imprimirReporte() {
        val pdfFile = generarPdf() ?: return
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val adapter = PdfPrintDocumentAdapter(pdfFile, "reporte_css")
        printManager.print("Reporte CSS", adapter, PrintAttributes.Builder().build())
    }

    private fun formatoMoneda(valor: Double): String =
        String.format(Locale("es", "PA"), "B/. %.2f", valor)

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