package com.prospera.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.prospera.app.R
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.entities.EmpresaEntity
import com.prospera.app.data.entities.PreferenciasEntity
import com.prospera.app.data.repository.AuthRepository
import com.prospera.app.data.repository.PreferenciasRepository
import com.prospera.app.utils.SessionManager
import kotlinx.coroutines.launch

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var repo: PreferenciasRepository
    private lateinit var authRepo: AuthRepository
    private var prefsActuales: PreferenciasEntity = PreferenciasEntity()
    private var empresaActual: EmpresaEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        repo = PreferenciasRepository(applicationContext)
        authRepo = AuthRepository(applicationContext)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        cargarPreferencias()
        cargarEmpresa()
        configurarPerfil()
        configurarTasasLegales()
        configurarDatosEmpresa()
        configurarCerrarSesion()
    }

    private fun cargarPreferencias() {
        lifecycleScope.launch {
            prefsActuales = repo.obtener()

            if (prefsActuales.correoUsuario.isBlank()) {
                val usuarioId = SessionManager.getUsuarioId(this@ConfiguracionActivity)
                val usuario = authRepo.usuarioDao.buscarPorId(usuarioId)
                if (usuario != null) {
                    prefsActuales = prefsActuales.copy(
                        nombreUsuario = usuario.nombre,
                        correoUsuario = usuario.email
                    )
                }
            }

            pintarUI(prefsActuales)
        }
    }

    private fun cargarEmpresa() {
        lifecycleScope.launch {
            val empresaId = SessionManager.getEmpresaId(this@ConfiguracionActivity)
            val empresa = AppDatabase.getInstance(applicationContext).empresaDao().buscarPorId(empresaId)
                ?: return@launch
            empresaActual = empresa
            findViewById<EditText>(R.id.etHorasSemanales).setText(empresa.horasSemanales.toString())
        }
    }

    private fun pintarUI(prefs: PreferenciasEntity) {
        findViewById<EditText>(R.id.etNombre).setText(prefs.nombreUsuario)
        findViewById<EditText>(R.id.etCorreo).setText(prefs.correoUsuario)

        findViewById<MaterialSwitch>(R.id.switchModoOscuro).apply {
            isChecked = prefs.modoOscuro
            setOnCheckedChangeListener { _, activo ->
                lifecycleScope.launch { repo.actualizarModoOscuro(activo) }
                aplicarModoOscuro(activo)
            }
        }

        findViewById<MaterialSwitch>(R.id.switchNotificaciones).apply {
            isChecked = prefs.notificacionesActivas
            setOnCheckedChangeListener { _, activo ->
                lifecycleScope.launch { repo.actualizarNotificaciones(activo) }
            }
        }
        findViewById<android.view.View>(R.id.btnGestionarUsuarios).setOnClickListener {
            startActivity(Intent(this, GestionarUsuariosActivity::class.java))
        }
        findViewById<EditText>(R.id.etCssEmpleado).setText((prefs.cssEmpleado * 100).toString())
        findViewById<EditText>(R.id.etSegEducativo).setText((prefs.segEducativo * 100).toString())
        findViewById<EditText>(R.id.etIsrDeduccion).setText(prefs.isrDeduccionCasado.toString())
    }

    private fun aplicarModoOscuro(activo: Boolean) {
        val modo = if (activo) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        } else {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(modo)
    }

    private fun configurarPerfil() {
        findViewById<Button>(R.id.btnGuardarPerfil).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombre).text.toString().trim()
            val correo = findViewById<EditText>(R.id.etCorreo).text.toString().trim()

            if (nombre.isBlank()) {
                findViewById<EditText>(R.id.etNombre).error = "Requerido"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                repo.actualizarPerfil(nombre, correo)
            }
        }
    }

    private fun configurarDatosEmpresa() {
        findViewById<Button>(R.id.btnGuardarEmpresa).setOnClickListener {
            val etHoras = findViewById<EditText>(R.id.etHorasSemanales)
            val horas = etHoras.text.toString().toDoubleOrNull()

            if (horas == null || horas <= 0) {
                etHoras.error = "Requerido"
                return@setOnClickListener
            }

            val empresa = empresaActual ?: return@setOnClickListener
            lifecycleScope.launch {
                val actualizada = empresa.copy(horasSemanales = horas)
                AppDatabase.getInstance(applicationContext).empresaDao().actualizar(actualizada)
                empresaActual = actualizada
            }
        }
    }

    private fun configurarTasasLegales() {
        findViewById<Button>(R.id.btnGuardarTasas).setOnClickListener {
            val css = findViewById<EditText>(R.id.etCssEmpleado).text.toString()
                .toDoubleOrNull()?.div(100) ?: prefsActuales.cssEmpleado
            val segEdu = findViewById<EditText>(R.id.etSegEducativo).text.toString()
                .toDoubleOrNull()?.div(100) ?: prefsActuales.segEducativo
            val isrDed = findViewById<EditText>(R.id.etIsrDeduccion).text.toString()
                .toDoubleOrNull() ?: prefsActuales.isrDeduccionCasado

            lifecycleScope.launch {
                repo.actualizarTasasLegales(css, segEdu, isrDed)
            }
        }
    }

    private fun configurarCerrarSesion() {
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            SessionManager.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}