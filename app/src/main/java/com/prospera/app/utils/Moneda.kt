// utils/Moneda.kt
package com.prospera.app.utils

import java.text.NumberFormat
import java.util.Locale

object Moneda {
    // Panamá usa B/. con formato igual a USD (2 decimales, punto decimal)
    private val formato = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatear(valor: Double): String = "B/. ${formato.format(valor)}"
}