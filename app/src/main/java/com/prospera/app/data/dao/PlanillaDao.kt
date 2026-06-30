package com.prospera.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prospera.app.data.ConsolidadoMensualRow
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.entities.PlanillaEntity

@Dao
interface PlanillaDao {

    @Insert
    suspend fun insertarPlanilla(planilla: PlanillaEntity): Long

    @Update
    suspend fun actualizarPlanilla(planilla: PlanillaEntity)

    @Query("""
        SELECT * FROM planillas
        WHERE empresaId = :empresaId AND periodo = :periodo
          AND mes = :mes AND anio = :anio
        LIMIT 1
    """)
    suspend fun buscar(empresaId: Long, periodo: String, mes: Int, anio: Int): PlanillaEntity?

    @Query("SELECT * FROM planillas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): PlanillaEntity?

    @Query("SELECT * FROM planillas WHERE empresaId = :empresaId ORDER BY anio DESC, mes DESC, periodo DESC")
    suspend fun listarPorEmpresa(empresaId: Long): List<PlanillaEntity>

    @Insert
    suspend fun insertarDetalles(detalles: List<DetallePlanillaEntity>)

    @Insert
    suspend fun insertarDetalle(detalle: DetallePlanillaEntity): Long

    @Update
    suspend fun actualizarDetalle(detalle: DetallePlanillaEntity)

    @Query("SELECT * FROM detalle_planilla WHERE planillaId = :planillaId")
    suspend fun detallesDePlanilla(planillaId: Long): List<DetallePlanillaEntity>

    @Query("SELECT * FROM detalle_planilla WHERE id = :id LIMIT 1")
    suspend fun buscarDetallePorId(id: Long): DetallePlanillaEntity?

    @Query("""
        SELECT e.id AS empleadoId,
               SUM(dp.salarioBruto) AS brutoMes,
               SUM(dp.totalDescuentos) AS descuentosMes,
               SUM(dp.salarioNeto) AS netoMes
        FROM detalle_planilla dp
        JOIN planillas p ON p.id = dp.planillaId
        JOIN empleados e ON e.id = dp.empleadoId
        WHERE p.empresaId = :empresaId AND p.mes = :mes AND p.anio = :anio
          AND p.estado = 'pagada'
        GROUP BY e.id
    """)
    suspend fun consolidadoMensual(empresaId: Long, mes: Int, anio: Int): List<ConsolidadoMensualRow>
}