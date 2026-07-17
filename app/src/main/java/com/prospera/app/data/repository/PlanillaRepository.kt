package com.prospera.app.data.repository

import com.prospera.app.data.AporteCssRow
import com.prospera.app.data.ConsolidadoMensualRow
import com.prospera.app.data.dao.EmpleadoDao
import com.prospera.app.data.dao.EmpresaDao
import com.prospera.app.data.dao.PlanillaDao
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.data.entities.EmpresaEntity
import com.prospera.app.data.entities.PlanillaEntity
import com.prospera.app.data.entities.PreferenciasEntity
import com.prospera.app.helpers.CalculadoraPlanilla
import com.prospera.app.helpers.IngresoInput

class PlanillaRepository(
    private val planillaDao: PlanillaDao,
    private val empleadoDao: EmpleadoDao,
    private val empresaDao: EmpresaDao,
    private val preferenciasRepository: PreferenciasRepository,
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
        val prefs = preferenciasRepository.obtener()

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
                prefs = prefs,
                horasExtraDiurnas = 0.0,
                horasExtraNocturnas = 0.0,
                montoComision = 0.0,
                montoDietas = 0.0,
                montoPrima = 0.0,
                descMuebleria = 0.0,
                descAdelanto = 0.0,
                descAhorro = 0.0
            )
        }
        if (detalles.isNotEmpty()) planillaDao.insertarDetalles(detalles)

        return planillaDao.buscarPorId(nuevaId)!!
    }

    suspend fun actualizarLinea(
        detalleId: Long,
        horasExtraDiurnas: Double,
        horasExtraNocturnas: Double,
        montoComision: Double,
        montoDietas: Double,
        montoPrima: Double,
        descMuebleria: Double,
        descAdelanto: Double,
        descAhorro: Double
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
        val prefs = preferenciasRepository.obtener()

        val recalculado = construirDetalle(
            planillaId = detalleActual.planillaId,
            empleado = empleado,
            empresa = empresa,
            prefs = prefs,
            horasExtraDiurnas = horasExtraDiurnas,
            horasExtraNocturnas = horasExtraNocturnas,
            montoComision = montoComision,
            montoDietas = montoDietas,
            montoPrima = montoPrima,
            descMuebleria = descMuebleria,
            descAdelanto = descAdelanto,
            descAhorro = descAhorro
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

    suspend fun reporteCssMensual(empresaId: Long, mes: Int, anio: Int): List<AporteCssRow> =
        planillaDao.reporteCssMensual(empresaId, mes, anio)
    // --- privado ---

    private fun construirDetalle(
        planillaId: Long,
        empleado: EmpleadoEntity,
        empresa: EmpresaEntity,
        prefs: PreferenciasEntity,
        horasExtraDiurnas: Double,
        horasExtraNocturnas: Double,
        montoComision: Double,
        montoDietas: Double,
        montoPrima: Double,
        descMuebleria: Double,
        descAdelanto: Double,
        descAhorro: Double
    ): DetallePlanillaEntity {
        val ingresos = buildList {
            if (montoComision > 0) add(IngresoInput(tipo = "comision", monto = montoComision))
            if (montoDietas > 0) add(IngresoInput(tipo = "dietas", monto = montoDietas))
            if (montoPrima > 0) add(IngresoInput(tipo = "prima", monto = montoPrima))
        }

        val resultado = calculadora.calcularQuincena(
            salarioBase = empleado.salarioBase,
            estadoCivil = empleado.estadoCivil,
            horasSemanales = empresa.horasSemanales,
            semanasMes = empresa.semanasMes,
            horasExtraDiurnas = horasExtraDiurnas,
            horasExtraNocturnas = horasExtraNocturnas,
            ingresos = ingresos,
            descMuebleria = descMuebleria,
            descAdelanto = descAdelanto,
            descAhorro = descAhorro,
            cssEmpleado = prefs.cssEmpleado,
            segEducativo = prefs.segEducativo,
            isrDeduccionCasado = prefs.isrDeduccionCasado
        )

        return DetallePlanillaEntity(
            planillaId = planillaId,
            empleadoId = empleado.id,
            horasExtraDiurnas = horasExtraDiurnas,
            horasExtraNocturnas = horasExtraNocturnas,
            montoComision = montoComision,
            montoDietas = montoDietas,
            montoPrima = montoPrima,
            descMuebleria = descMuebleria,
            descAdelanto = descAdelanto,
            descAhorro = descAhorro,
            salarioBaseQuincena = resultado.salarioBaseQuincena,
            valorHora = resultado.valorHora,
            montoHorasExtrasCalculado = resultado.montoHorasExtras,
            montoBonificacion = resultado.montoBonificacion,
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