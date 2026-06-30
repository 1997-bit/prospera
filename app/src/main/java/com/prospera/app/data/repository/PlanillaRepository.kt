package com.prospera.app.data.repository

import com.prospera.app.data.ConsolidadoMensualRow
import com.prospera.app.data.dao.EmpleadoDao
import com.prospera.app.data.dao.EmpresaDao
import com.prospera.app.data.dao.PlanillaDao
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.data.entities.EmpresaEntity
import com.prospera.app.data.entities.PlanillaEntity
import com.prospera.app.helpers.CalculadoraPlanilla
import com.prospera.app.helpers.IngresoInput

class PlanillaRepository(
    private val planillaDao: PlanillaDao,
    private val empleadoDao: EmpleadoDao,
    private val empresaDao: EmpresaDao,
    private val calculadora: CalculadoraPlanilla = CalculadoraPlanilla()
) {

    suspend fun generarOAbrir(
        empresaId: Long,
        periodo: String,
        mes: Int,
        anio: Int
    ): PlanillaEntity {
        planillaDao.buscar(empresaId, periodo, mes, anio)?.let { return it }

        val empresa = empresaDao.buscarPorId(empresaId)
            ?: error("Empresa $empresaId no encontrada")

        val nuevaId = planillaDao.insertarPlanilla(
            PlanillaEntity(
                empresaId = empresaId,
                periodo = periodo,
                mes = mes,
                anio = anio,
                estado = "borrador"
            )
        )

        val empleados = empleadoDao.listarActivos(empresaId)
        val detalles = empleados.map { empleado ->
            construirDetalle(
                planillaId = nuevaId,
                empleado = empleado,
                empresa = empresa,
                montoHorasExtrasInput = 0.0,
                montoComision = 0.0,
                montoDietas = 0.0,
                montoPrima = 0.0,
                otrosDescuentosInput = 0.0
            )
        }
        if (detalles.isNotEmpty()) planillaDao.insertarDetalles(detalles)

        return planillaDao.buscarPorId(nuevaId)!!
    }

    suspend fun actualizarLinea(
        detalleId: Long,
        montoHorasExtrasInput: Double,
        montoComision: Double,
        montoDietas: Double,
        montoPrima: Double,
        otrosDescuentosInput: Double
    ) {
        val detalleActual = planillaDao.buscarDetallePorId(detalleId)
            ?: error("Detalle $detalleId no encontrado")
        val planilla = planillaDao.buscarPorId(detalleActual.planillaId)
            ?: error("Planilla ${detalleActual.planillaId} no encontrada")
        check(planilla.estado != "pagada") { "No se puede editar una planilla pagada" }

        val empleado = empleadoDao.buscarPorId(detalleActual.empleadoId)
            ?: error("Empleado ${detalleActual.empleadoId} no encontrado")
        val empresa = empresaDao.buscarPorId(planilla.empresaId)
            ?: error("Empresa ${planilla.empresaId} no encontrada")

        val recalculado = construirDetalle(
            planillaId = detalleActual.planillaId,
            empleado = empleado,
            empresa = empresa,
            montoHorasExtrasInput = montoHorasExtrasInput,
            montoComision = montoComision,
            montoDietas = montoDietas,
            montoPrima = montoPrima,
            otrosDescuentosInput = otrosDescuentosInput
        ).copy(id = detalleId)

        planillaDao.actualizarDetalle(recalculado)
    }

    suspend fun marcarPagada(planillaId: Long) {
        val planilla = planillaDao.buscarPorId(planillaId)
            ?: error("Planilla $planillaId no encontrada")
        check(planilla.estado == "borrador") { "La planilla ya está pagada" }

        planillaDao.actualizarPlanilla(
            planilla.copy(estado = "pagada", fechaPago = System.currentTimeMillis())
        )
    }

    suspend fun detallesDePlanilla(planillaId: Long): List<DetallePlanillaEntity> =
        planillaDao.detallesDePlanilla(planillaId)

    suspend fun consolidadoMensual(empresaId: Long, mes: Int, anio: Int): List<ConsolidadoMensualRow> =
        planillaDao.consolidadoMensual(empresaId, mes, anio)

    // --- privado ---

    private fun construirDetalle(
        planillaId: Long,
        empleado: EmpleadoEntity,
        empresa: EmpresaEntity,
        montoHorasExtrasInput: Double,
        montoComision: Double,
        montoDietas: Double,
        montoPrima: Double,
        otrosDescuentosInput: Double
    ): DetallePlanillaEntity {
        // horas_extra entra como ingreso gravable directo: monto ya viene calculado a mano
        val ingresos = buildList {
            if (montoHorasExtrasInput > 0) add(IngresoInput(tipo = "horas_extra", monto = montoHorasExtrasInput))
            if (montoComision > 0) add(IngresoInput(tipo = "comision", monto = montoComision))
            if (montoDietas > 0) add(IngresoInput(tipo = "dietas", monto = montoDietas))
            if (montoPrima > 0) add(IngresoInput(tipo = "prima", monto = montoPrima))
        }

        val resultado = calculadora.calcularQuincena(
            salarioBase = empleado.salarioBase,
            estadoCivil = empleado.estadoCivil,
            horasSemanales = empresa.horasSemanales,
            semanasMes = empresa.semanasMes,
            ingresos = ingresos,
            otrosDescuentos = otrosDescuentosInput
        )

        return DetallePlanillaEntity(
            planillaId = planillaId,
            empleadoId = empleado.id,
            montoHorasExtrasInput = montoHorasExtrasInput,
            montoComision = montoComision,
            montoDietas = montoDietas,
            montoPrima = montoPrima,
            otrosDescuentosInput = otrosDescuentosInput,
            salarioBaseQuincena = resultado.salarioBaseQuincena,
            valorHora = resultado.valorHora,
            otrosIngresosGravables = resultado.otrosIngresos,
            otrosIngresosSinDescuento = resultado.otrosIngresosSinDescuento,
            salarioBruto = resultado.salarioBruto,
            descSeguroSocial = resultado.descSeguroSocial,
            descSeguroEducativo = resultado.descSeguroEducativo,
            descISR = resultado.descISR,
            otrosDescuentos = resultado.otrosDescuentos,
            totalDescuentos = resultado.totalDescuentos,
            salarioNeto = resultado.salarioNeto,
            alertaDescExcede = resultado.alertaDescExcede
        )
    }
}