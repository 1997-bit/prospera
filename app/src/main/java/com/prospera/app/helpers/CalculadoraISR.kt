package com.prospera.app.helpers

import com.prospera.app.utils.Constants
import java.math.BigDecimal
import java.math.RoundingMode

class CalculadoraISR {

    private fun redondear(valor: Double, decimales: Int = 2): Double =
        BigDecimal(valor).setScale(decimales, RoundingMode.HALF_UP).toDouble()

    /**
     * Calcula ISR quincenal a partir de ingreso bruto quincenal.
     * rentaAnual = brutoQuincena * 2 * 13 (24 quincenas + décimo)
     * Normativa: DGI Panamá, Art.10, Ley 8/15-mar-2010
     */
    fun calcularQuincena(brutoQuincena: Double, estadoCivil: String): Double {
        val rentaAnual = brutoQuincena * 2 * 13

        if (rentaAnual <= Constants.ISR_TRAMO1_TOPE) return 0.0

        val isrAnual = if (rentaAnual <= Constants.ISR_TRAMO2_TOPE) {
            (rentaAnual - Constants.ISR_TRAMO1_TOPE) * Constants.ISR_TRAMO2_TASA
        } else {
            Constants.ISR_TRAMO3_BASE + (rentaAnual - Constants.ISR_TRAMO2_TOPE) * Constants.ISR_TRAMO3_TASA
        }

        val deduccion = if (estadoCivil in listOf("casado", "unido"))
            (Constants.ISR_DEDUCCION_E * Constants.ISR_TRAMO2_TASA) / 24
        else 0.0

        return redondear(maxOf(0.0, (isrAnual / 24) - deduccion))
    }
}