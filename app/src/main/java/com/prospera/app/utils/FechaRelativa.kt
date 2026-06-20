// utils/FechaRelativa.kt
package com.prospera.app.utils

import java.text.SimpleDateFormat
import java.util.*

object FechaRelativa {

    /** "Hoy, 10:32 AM" / "Ayer, 3:15 PM" / "02 Jun, 9:00 AM" */
    fun formatear(timestampMillis: Long): String {
        val ahora = Calendar.getInstance()
        val fecha = Calendar.getInstance().apply { timeInMillis = timestampMillis }

        val horaFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val mismoDia = ahora.get(Calendar.YEAR) == fecha.get(Calendar.YEAR) &&
                ahora.get(Calendar.DAY_OF_YEAR) == fecha.get(Calendar.DAY_OF_YEAR)

        val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val esAyer = ayer.get(Calendar.YEAR) == fecha.get(Calendar.YEAR) &&
                ayer.get(Calendar.DAY_OF_YEAR) == fecha.get(Calendar.DAY_OF_YEAR)

        val prefijo = when {
            mismoDia -> "Hoy"
            esAyer -> "Ayer"
            else -> SimpleDateFormat("dd MMM", Locale("es", "PA")).format(fecha.time)
        }

        return "$prefijo, ${horaFmt.format(fecha.time)}"
    }
}