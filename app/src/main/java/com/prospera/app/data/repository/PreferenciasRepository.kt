package com.prospera.app.data.repository

import android.content.Context
import com.prospera.app.data.AppDatabase
import com.prospera.app.data.entities.PreferenciasEntity
import kotlinx.coroutines.flow.Flow

class PreferenciasRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).preferenciasDao()

    fun observar(): Flow<PreferenciasEntity?> = dao.observar()

    suspend fun obtener(): PreferenciasEntity =
        dao.obtener() ?: PreferenciasEntity().also { dao.guardar(it) }

    suspend fun actualizarModoOscuro(activo: Boolean) {
        val actual = obtener()
        dao.guardar(actual.copy(modoOscuro = activo))
    }

    suspend fun actualizarNotificaciones(activo: Boolean) {
        val actual = obtener()
        dao.guardar(actual.copy(notificacionesActivas = activo))
    }

    suspend fun actualizarPerfil(nombre: String, correo: String) {
        val actual = obtener()
        dao.guardar(actual.copy(nombreUsuario = nombre, correoUsuario = correo))
    }

    suspend fun actualizarTasasLegales(css: Double, segEdu: Double, isrDeduccion: Double) {
        val actual = obtener()
        dao.guardar(actual.copy(
            cssEmpleado = css,
            segEducativo = segEdu,
            isrDeduccionCasado = isrDeduccion
        ))
    }
}