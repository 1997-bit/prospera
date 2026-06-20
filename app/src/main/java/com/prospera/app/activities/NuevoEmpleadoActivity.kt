package com.prospera.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prospera.app.R

class NuevoEmpleadoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_empleado)

        findViewById<android.widget.Button>(R.id.btnGuardar).setOnClickListener {
            // TODO: persistir en Room (próximo paso)
            finish()
        }
    }
}