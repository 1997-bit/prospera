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
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.prospera.app.R
import com.prospera.app.adapters.ReporteColaboradorAdapter
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.helpers.GeneradorPdfReporte
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ReporteColaboradoresActivity : AppCompatActivity() {

    private lateinit var adapter: ReporteColaboradorAdapter
    private var empresaId: Long = 0
    private var colaboradoresActuales: List<EmpleadoEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_colaboradores)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        empresaId = SessionManager.getEmpresaId(this)

        adapter = ReporteColaboradorAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvColaboradores).apply {
            layoutManager = LinearLayoutManager(this@ReporteColaboradoresActivity)
            adapter = this@ReporteColaboradoresActivity.adapter
        }

        cargarColaboradores()

        findViewById<MaterialButton>(R.id.btnImprimir).setOnClickListener { imprimirReporte() }
        findViewById<MaterialButton>(R.id.btnEnviarCorreo).setOnClickListener { enviarPorCorreo() }
    }

    private fun cargarColaboradores() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            colaboradoresActuales = db.empleadoDao().listarTodos(empresaId)
            adapter.actualizarLista(colaboradoresActuales)

            val activos = colaboradoresActuales.count { it.activo }
            val inactivos = colaboradoresActuales.size - activos
            findViewById<TextView>(R.id.tvTotalActivos).text = activos.toString()
            findViewById<TextView>(R.id.tvTotalInactivos).text = inactivos.toString()

            findViewById<RecyclerView>(R.id.rvColaboradores).visibility =
                if (colaboradoresActuales.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            findViewById<TextView>(R.id.tvSinDatos).visibility =
                if (colaboradoresActuales.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun generarPdf(): File? {
        if (colaboradoresActuales.isEmpty()) {
            Toast.makeText(this, "No hay colaboradores para generar el reporte", Toast.LENGTH_SHORT).show()
            return null
        }
        return GeneradorPdfReporte.generarReporteColaboradores(this, colaboradoresActuales)
    }

    private fun enviarPorCorreo() {
        val pdfFile = generarPdf() ?: return
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de colaboradores")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar reporte"))
    }

    private fun imprimirReporte() {
        val pdfFile = generarPdf() ?: return
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val adapter = PdfPrintDocumentAdapter(pdfFile, "colaboradores")
        printManager.print("Reporte de colaboradores", adapter, PrintAttributes.Builder().build())
    }

    /** Igual al de ReportePlanillaConsolidadaActivity — reutiliza ese patrón. */
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