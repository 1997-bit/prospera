package com.prospera.app.data

import java.util.Calendar
import java.util.Locale

/**
 * Modelos de UI / agregacion. NO son @Entity → no son tablas en la DB.
 * Salen de queries (JOIN/SUM) o son DTOs simples para pantallas.
 */

data class ActividadReciente(
    val descripcion: String,
    val timestampMillis: Long
)

data class ConsolidadoMensualRow(
    val empleadoId: Long,
    val brutoMes: Double,
    val descuentosMes: Double,
    val netoMes: Double
)

data class UsuarioConRol(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String
)

data class ResumenMensual(
    val mesAnio: String, // "Junio 2026"
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
