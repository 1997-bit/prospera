package com.prospera.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prospera.app.R

class PlanillaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planilla)

        findViewById<android.widget.Button>(R.id.btnCalcular).setOnClickListener {
            // TODO: usar CalculadoraPlanilla por cada empleado de Room
        }
    }
}