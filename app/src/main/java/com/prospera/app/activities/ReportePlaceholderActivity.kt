// ReportePlaceholderActivity.kt
// Reemplaza a: ReportePlanillaActivity, ReporteColaboradoresActivity,
// ReporteIndividualActivity, ReporteCSSActivity (eran 4 clases idénticas,
// solo cambiaba el string del título).
package com.prospera.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prospera.app.R

class ReportePlaceholderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITULO_RES_ID = "extra_titulo_res_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_placeholder)

        val tituloResId = intent.getIntExtra(EXTRA_TITULO_RES_ID, R.string.reportes_titulo)
        findViewById<android.widget.TextView>(R.id.tvTituloReporte).text = getString(tituloResId)
    }
}
