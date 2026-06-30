package com.prospera.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.prospera.app.data.repository.PreferenciasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProsperaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        aplicarModoOscuroGuardado()
    }

    private fun aplicarModoOscuroGuardado() {
        CoroutineScope(Dispatchers.Main).launch {
            val repo = PreferenciasRepository(applicationContext)
            val prefs = repo.obtener()
            val modo = if (prefs.modoOscuro) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(modo)
        }
    }
}