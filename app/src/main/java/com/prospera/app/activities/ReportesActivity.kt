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

        findViewById<android.view.View>(R.id.cardReportePlanilla).setOnClickListener {
            startActivity(Intent(this, ReportePlanillaActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardReporteColaboradores).setOnClickListener {
            startActivity(Intent(this, ReporteColaboradoresActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardReporteIndividual).setOnClickListener {
            startActivity(Intent(this, ReporteIndividualActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardReporteCSS).setOnClickListener {
            startActivity(Intent(this, ReporteCSSActivity::class.java))
        }
    }
}