package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.prospera.app.R

class ReportesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        abrirReporteAlTocar(R.id.cardReportePlanilla, R.string.reporte_planilla_nombre)
        abrirReporteAlTocar(R.id.cardReporteColaboradores, R.string.reporte_colaboradores_nombre)
        abrirReporteAlTocar(R.id.cardReporteIndividual, R.string.reporte_individual_nombre)
        abrirReporteAlTocar(R.id.cardReporteCSS, R.string.reporte_css_nombre)
    }

    private fun abrirReporteAlTocar(cardId: Int, tituloResId: Int) {
        findViewById<android.view.View>(cardId).setOnClickListener {
            val intent = Intent(this, ReportePlaceholderActivity::class.java)
            intent.putExtra(ReportePlaceholderActivity.EXTRA_TITULO_RES_ID, tituloResId)
            startActivity(intent)
        }
    }
}