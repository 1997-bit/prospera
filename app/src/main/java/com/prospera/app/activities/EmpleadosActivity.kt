package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prospera.app.R

class EmpleadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empleados)

        findViewById<android.view.View>(R.id.fabAgregar).setOnClickListener {
            startActivity(Intent(this, NuevoEmpleadoActivity::class.java))
        }
    }
}