package com.prospera.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class EmpleadoRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).empleadoDao()

    fun observarActivos(empresaId: Long): Flow<List<EmpleadoEntity>> = dao.observarActivos(empresaId)

    suspend fun listarActivos(empresaId: Long) = dao.listarActivos(empresaId)

    suspend fun buscar(empresaId: Long, query: String) =
        if (query.isBlank()) dao.listarActivos(empresaId) else dao.buscar(empresaId, query)

    suspend fun obtener(id: Long) = dao.buscarPorId(id)

    suspend fun guardar(
        empresaId: Long,
        nombre: String,
        cedula: String,
        cargo: String,
        departamento: String,
        estadoCivil: String,
        salarioBase: Double,
        anioInicio: Int,
        idExistente: Long? = null
    ): Long {
        return if (idExistente == null) {
            dao.insertar(
                EmpleadoEntity(
                    empresaId = empresaId, nombre = nombre, cedula = cedula,
                    cargo = cargo, departamento = departamento, estadoCivil = estadoCivil,
                    salarioBase = salarioBase, anioInicio = anioInicio
                )
            )
        } else {
            val existente = dao.buscarPorId(idExistente) ?: return -1
            dao.actualizar(
                existente.copy(
                    nombre = nombre, cedula = cedula, cargo = cargo,
                    departamento = departamento, estadoCivil = estadoCivil,
                    salarioBase = salarioBase, anioInicio = anioInicio
                )
            )
            idExistente
        }
    }

    suspend fun eliminar(id: Long) {
        dao.buscarPorId(id)?.let { dao.eliminar(it) }
    }

    suspend fun cedulaDisponible(cedula: String, empresaId: Long, idActual: Long? = null): Boolean {
        val existente = dao.buscarPorCedula(cedula, empresaId)
        return existente == null || existente.id == idActual
    }
}