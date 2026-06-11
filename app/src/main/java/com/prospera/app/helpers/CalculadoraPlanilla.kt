package com.prospera.app.helpers

import com.prospera.app.utils.Constants
import java.math.BigDecimal
import java.math.RoundingMode

data class IngresoInput(
    val tipo: String,
    val monto: Double,
    val horas: Double? = null
)

data class DetalleIngreso(
    val tipo: String,
    val monto: Double,
    val gravable: Double,
    val sinDescuento: Double,
    val horas: Double?
)

data class ResultadoIngresos(
    val totalGravable: Double,
    val totalSinDescuento: Double,
    val excedenteCSS: Double,
    val detalle: List<DetalleIngreso>
)

data class ResultadoPlanilla(
    // Ingresos
    val salarioBaseQuincena: Double,
    val valorHora: Double,
    val otrosIngresos: Double,
    val otrosIngresosSinDescuento: Double,
    val detalleIngresos: List<DetalleIngreso>,
    val salarioBruto: Double,
    // Deducciones
    val descSeguroSocial: Double,
    val descSeguroEducativo: Double,
    val descISR: Double,
    val otrosDescuentos: Double,
    val totalDescuentos: Double,
    val salarioNeto: Double,
    // Alerta
    val alertaDescExcede: Boolean,
    val pctDescuentos: Double
)

class CalculadoraPlanilla(
    private val calculadoraIsr: CalculadoraISR = CalculadoraISR()
) {

    private fun redondear(valor: Double, decimales: Int = 2): Double =
        BigDecimal(valor).setScale(decimales, RoundingMode.HALF_UP).toDouble()

    /**
     * Calcula línea completa de planilla quincenal.
     * @param salarioBase salario mensual base
     * @param estadoCivil "casado", "unido", o "soltero"
     * @param horasSemanales default 48
     * @param semanasMes default 4.3333
     * @param ingresos lista de ingresos variables del período
     * @param otrosDescuentos descuentos adicionales (mueblería, adelantos, etc.)
     */
    fun calcularQuincena(
        salarioBase: Double,
        estadoCivil: String = "soltero",
        horasSemanales: Double = 48.0,
        semanasMes: Double = 4.3333,
        ingresos: List<IngresoInput> = emptyList(),
        otrosDescuentos: Double = 0.0
    ): ResultadoPlanilla {

        val quincena = redondear(salarioBase / 2)
        val valorHora = redondear(salarioBase / (horasSemanales * semanasMes), 4)

        val resultadoIngresos = procesarIngresos(ingresos, salarioBase, valorHora)

        val bruto = redondear(quincena + resultadoIngresos.totalGravable)
        val baseCSS = redondear(bruto + resultadoIngresos.excedenteCSS)

        val css = redondear(baseCSS * Constants.CSS_EMPLEADO)
        val segEdu = redondear(baseCSS * Constants.SEG_EDUCATIVO)
        val isr = calculadoraIsr.calcularQuincena(bruto, estadoCivil)

        val otrosDescAjustados = redondear(minOf(otrosDescuentos, bruto * Constants.MAX_OTROS_DESC_PCT))
        val totalDesc = redondear(css + segEdu + isr + otrosDescAjustados)

        val neto = redondear((bruto - totalDesc) + resultadoIngresos.totalSinDescuento)

        val pctDesc = if (bruto > 0) otrosDescAjustados / bruto else 0.0
        val alertaDesc = pctDesc > Constants.MAX_OTROS_DESC_PCT

        return ResultadoPlanilla(
            salarioBaseQuincena = quincena,
            valorHora = valorHora,
            otrosIngresos = resultadoIngresos.totalGravable,
            otrosIngresosSinDescuento = resultadoIngresos.totalSinDescuento,
            detalleIngresos = resultadoIngresos.detalle,
            salarioBruto = bruto,
            descSeguroSocial = css,
            descSeguroEducativo = segEdu,
            descISR = isr,
            otrosDescuentos = otrosDescAjustados,
            totalDescuentos = totalDesc,
            salarioNeto = neto,
            alertaDescExcede = alertaDesc,
            pctDescuentos = redondear(pctDesc * 100)
        )
    }

    private fun procesarIngresos(
        ingresos: List<IngresoInput>,
        salMensual: Double,
        valorHora: Double
    ): ResultadoIngresos {
        var totalGravable = 0.0
        var totalSinDescuento = 0.0
        var excedenteCSS = 0.0
        val detalle = mutableListOf<DetalleIngreso>()

        for (ingreso in ingresos) {
            val resultado = when (ingreso.tipo) {
                "comision", "bonificacion" -> calcularComision(ingreso.monto)
                "horas_extra" -> Triple(ingreso.monto, 0.0, 0.0)
                "dietas" -> calcularConExencion(ingreso.monto, salMensual * Constants.DIETAS_EXENCION)
                "prima" -> calcularConExencion(ingreso.monto, salMensual * Constants.PRIMA_EXENCION)
                else -> Triple(ingreso.monto, 0.0, 0.0)
            }

            totalGravable += resultado.first
            totalSinDescuento += resultado.second
            excedenteCSS += resultado.third

            detalle.add(DetalleIngreso(
                tipo = ingreso.tipo,
                monto = ingreso.monto,
                gravable = redondear(resultado.first),
                sinDescuento = redondear(resultado.second),
                horas = ingreso.horas
            ))
        }

        return ResultadoIngresos(
            totalGravable = redondear(totalGravable),
            totalSinDescuento = redondear(totalSinDescuento),
            excedenteCSS = redondear(excedenteCSS),
            detalle = detalle
        )
    }

    /** Comisión/bonificación: gravable 100% */
    private fun calcularComision(monto: Double): Triple<Double, Double, Double> =
        Triple(monto, 0.0, 0.0)

    /**
     * Ingreso con exención parcial (dietas, prima):
     * hasta umbral -> exento (sin_descuento); excedente -> gravable + CSS
     */
    private fun calcularConExencion(monto: Double, umbral: Double): Triple<Double, Double, Double> {
        val u = redondear(umbral)
        val excedente = redondear(maxOf(0.0, monto - u))
        val exento    = redondear(minOf(monto, u))
        return Triple(excedente, exento, excedente)
    }
}