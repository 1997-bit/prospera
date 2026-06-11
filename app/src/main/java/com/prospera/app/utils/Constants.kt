package com.prospera.app.utils

object Constants {

    // Salario
    const val SALARIO_MINIMO_MES = 655.15
    const val SALARIO_MINIMO_HORA = 3.36

    // Horas
    const val HORAS_SEMANALES = 45.0
    const val SEMANAS_MES = 4.333

    // Recargos HE
    const val RECARGO_DIURNO = 1.25
    const val RECARGO_NOCTURNO = 1.50
    const val RECARGO_DOMINICAL = 1.75

    // CSS empleado / patrono
    const val CSS_EMPLEADO = 0.0975
    const val CSS_PATRONO = 0.1325
    const val SEG_EDUCATIVO = 0.0125
    const val SEG_EDUCATIVO_PAT = 0.015

    // Ingresos con exención parcial
    const val DIETAS_EXENCION = 0.25   // 25% salario mensual
    const val PRIMA_EXENCION = 0.50   // 50% salario mensual
    const val COMISION_VENTAS = 0.02

    // Descuentos Art. 161 CT
    const val MAX_OTROS_DESC_PCT = 0.35
    const val MIN_NETO_PCT = 0.50

    // ISR tramos anuales — Ley 8/2010
    const val ISR_TRAMO1_TOPE = 11_000.0
    const val ISR_TRAMO2_TOPE = 50_000.0
    const val ISR_TRAMO2_TASA = 0.15
    const val ISR_TRAMO3_BASE = 5_850.0
    const val ISR_TRAMO3_TASA = 0.25
    const val ISR_DEDUCCION_E = 800.0 // casado/unido

    // Prestaciones
    const val DECIMO_PCT = 0.083333 // 1/12
    const val VACACIONES_PCT = 0.090909 // 1/11
    const val INDEMNIZACION_SEM = 3.4
}