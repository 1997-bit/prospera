package com.prospera.app.data

import java.util.Calendar
import java.util.Locale

data class ResumenMensual(
    val mesAnio: String,       // "Junio 2026"
    val totalBruto: Double,
    val totalDescuentos: Double,
    val totalNeto: Double,
    val colaboradoresActivos: Int
) {
    companion object {
        fun vacio(): ResumenMensual {
            val cal = Calendar.getInstance()
            val mes = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "PA"))
                ?.replaceFirstChar { it.uppercase() } ?: ""
            val anio = cal.get(Calendar.YEAR)
            return ResumenMensual("$mes $anio", 0.0, 0.0, 0.0, 0)
        }
    }
}