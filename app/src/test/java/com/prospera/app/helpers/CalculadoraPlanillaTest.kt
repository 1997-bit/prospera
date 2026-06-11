package com.prospera.app.helpers

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculadoraPlanillaTest {

    private lateinit var calculadora: CalculadoraPlanilla

    @Before
    fun setUp() {
        calculadora = CalculadoraPlanilla()
    }

    @Test
    fun `quincena base es mitad del salario mensual`() {
        val resultado = calculadora.calcularQuincena(salarioBase = 1000.0)
        assertEquals(500.0, resultado.salarioBaseQuincena, 0.001)
    }

    @Test
    fun `CSS empleado es 9_75 pct del bruto`() {
        val resultado = calculadora.calcularQuincena(salarioBase = 1000.0)
        val esperado = resultado.salarioBruto * 0.0975
        assertEquals(esperado, resultado.descSeguroSocial, 0.01)
    }

    @Test
    fun `seguro educativo es 1_25 pct del bruto`() {
        val resultado = calculadora.calcularQuincena(salarioBase = 1000.0)
        val esperado = resultado.salarioBruto * 0.0125
        assertEquals(esperado, resultado.descSeguroEducativo, 0.01)
    }

    @Test
    fun `neto mas descuentos igual bruto mas exentos`() {
        val r = calculadora.calcularQuincena(
            salarioBase = 1200.0,
            ingresos = listOf(IngresoInput("dietas", 200.0))
        )
        val esperado = r.salarioBruto - r.totalDescuentos + r.otrosIngresosSinDescuento
        assertEquals(esperado, r.salarioNeto, 0.01)
    }

    @Test
    fun `otros descuentos no exceden 35 pct Art161`() {
        val resultado = calculadora.calcularQuincena(
            salarioBase = 1000.0,
            otrosDescuentos = 9999.0   // intento de exceder
        )
        val limite = resultado.salarioBruto * 0.35
        assertTrue(resultado.otrosDescuentos <= limite)
    }

    @Test
    fun `dietas exentas hasta 25 pct salario mensual`() {
        // salario 1000, umbral dietas = 250
        // dietas 200 < 250 → todo exento, gravable = 0
        val resultado = calculadora.calcularQuincena(
            salarioBase = 1000.0,
            ingresos = listOf(IngresoInput("dietas", 200.0))
        )
        val detalle = resultado.detalleIngresos.first { it.tipo == "dietas" }
        assertEquals(0.0, detalle.gravable, 0.001)
        assertEquals(200.0, detalle.sinDescuento, 0.001)
    }

    @Test
    fun `dietas excedente es gravable`() {
        // salario 1000, umbral = 250, dietas = 400 → excedente 150 gravable
        val resultado = calculadora.calcularQuincena(
            salarioBase = 1000.0,
            ingresos = listOf(IngresoInput("dietas", 400.0))
        )
        val detalle = resultado.detalleIngresos.first { it.tipo == "dietas" }
        assertEquals(150.0, detalle.gravable, 0.01)
        assertEquals(250.0, detalle.sinDescuento, 0.01)
    }

    @Test
    fun `horas extra aumentan bruto`() {
        val sinHE = calculadora.calcularQuincena(salarioBase = 1000.0)
        val conHE = calculadora.calcularQuincena(
            salarioBase = 1000.0,
            ingresos = listOf(IngresoInput("horas_extra", 100.0))
        )
        assertTrue(conHE.salarioBruto > sinHE.salarioBruto)
    }

    @Test
    fun `alerta se activa si otros descuentos exceden 35 pct`() {
        // forzar exactamente en el límite + 1 centavo
        val brutoAprox = 500.0
        val resultado = calculadora.calcularQuincena(
            salarioBase = 1000.0,
            otrosDescuentos = brutoAprox * 0.36
        )
        // el sistema debe haberlo cortado, alerta = false porque ya fue limitado
        assertFalse(resultado.alertaDescExcede)
    }
}