package com.prospera.app.helpers

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculadoraISRTest {

    private lateinit var calculadora: CalculadoraISR

    @Before
    fun setUp() {
        calculadora = CalculadoraISR()
    }

    @Test
    fun `soltero bajo tramo1 retorna cero`() {
        // rentaAnual = 400 * 2 * 13 = 10_400 < 11_000
        val resultado = calculadora.calcularQuincena(400.0, "soltero")
        assertEquals(0.0, resultado, 0.001)
    }

    @Test
    fun `soltero tramo2 calcula correctamente`() {
        // rentaAnual = 500 * 2 * 13 = 13_000
        // isrAnual = (13_000 - 11_000) * 0.15 = 300
        // isrQuincena = 300 / 24 = 12.50
        val resultado = calculadora.calcularQuincena(500.0, "soltero")
        assertEquals(12.50, resultado, 0.01)
    }

    @Test
    fun `casado aplica deduccion`() {
        // misma base que arriba pero con deducción
        // deduccion = (800 * 0.15) / 24 = 5.0
        // resultado = 12.50 - 5.0 = 7.50
        val resultado = calculadora.calcularQuincena(500.0, "casado")
        assertEquals(7.50, resultado, 0.01)
    }

    @Test
    fun `unido aplica misma deduccion que casado`() {
        val casado = calculadora.calcularQuincena(500.0, "casado")
        val unido  = calculadora.calcularQuincena(500.0, "unido")
        assertEquals(casado, unido, 0.001)
    }

    @Test
    fun `tramo3 calcula correctamente`() {
        // rentaAnual = 2000 * 2 * 13 = 52_000 > 50_000
        // isrAnual = 5850 + (52_000 - 50_000) * 0.25 = 5850 + 500 = 6350
        // isrQuincena = 6350 / 24 = 264.58
        val resultado = calculadora.calcularQuincena(2000.0, "soltero")
        assertEquals(264.58, resultado, 0.01)
    }

    @Test
    fun `nunca retorna negativo`() {
        val resultado = calculadora.calcularQuincena(0.0, "casado")
        assertTrue(resultado >= 0.0)
    }
}