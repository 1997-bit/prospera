// ReporteIndividualActivity.kt
package com.prospera.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prospera.app.R

class ReporteIndividualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_placeholder)
        findViewById<android.widget.TextView>(R.id.tvTituloReporte).text =
            getString(R.string.reporte_individual_nombre)
    }
}