package com.prospera.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prospera.app.data.entities.EmpleadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpleadoDao {

    @Insert
    suspend fun insertar(empleado: EmpleadoEntity): Long

    @Update
    suspend fun actualizar(empleado: EmpleadoEntity)

    @Delete
    suspend fun eliminar(empleado: EmpleadoEntity)

    @Query("SELECT * FROM empleados WHERE empresaId = :empresaId AND activo = 1 ORDER BY nombre ASC")
    fun observarActivos(empresaId: Long): Flow<List<EmpleadoEntity>>

    @Query("SELECT * FROM empleados WHERE empresaId = :empresaId AND activo = 1 ORDER BY nombre ASC")
    suspend fun listarActivos(empresaId: Long): List<EmpleadoEntity>

    @Query("SELECT * FROM empleados WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): EmpleadoEntity?

    @Query("SELECT * FROM empleados WHERE cedula = :cedula AND empresaId = :empresaId LIMIT 1")
    suspend fun buscarPorCedula(cedula: String, empresaId: Long): EmpleadoEntity?

    @Query("""
        SELECT * FROM empleados
        WHERE empresaId = :empresaId AND activo = 1
        AND (nombre LIKE '%' || :query || '%'
             OR cedula LIKE '%' || :query || '%'
             OR cargo LIKE '%' || :query || '%')
        ORDER BY nombre ASC
    """)
    suspend fun buscar(empresaId: Long, query: String): List<EmpleadoEntity>

    @Query("SELECT COUNT(*) FROM empleados WHERE empresaId = :empresaId AND activo = 1")
    suspend fun contarActivos(empresaId: Long): Int
}