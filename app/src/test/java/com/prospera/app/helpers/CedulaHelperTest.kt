package com.prospera.app.helpers

import org.junit.Assert.*
import org.junit.Test

class CedulaHelperTest {

    // Simula escritura caracter por caracter — cada substring debe ser esValida=true
    private fun esValidaMientrasEscribe(cedula: String) {
        for (i in 0 until cedula.length) {
            val sub = cedula.substring(0, i)
            val r = CedulaHelper.validar(sub)
            assertTrue("fallo en: '$sub'", r.esValida)
        }
    }

    // Equivalente a isNotCompleteCedulaFor()
    private fun noEsCompletaMientrasEscribe(cedula: String) {
        for (i in 0 until cedula.length) {
            val sub = cedula.substring(0, i)
            val r = CedulaHelper.validar(sub)
            assertFalse("debería ser incompleta: '$sub'", r.esCompleta)
        }
    }

    //  Cédula vacía 
    @Test fun `vacia retorna esValida true`() {
        assertTrue(CedulaHelper.validar("").esValida)
    }

    //  Cédulas completas válidas 
    @Test fun `8-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("8-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("8-1234-12345", r.entrada)
        assertEquals("8", r.cedula!![0])
        assertEquals("", r.cedula[1])
        assertEquals("1234", r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    @Test fun `PE-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("PE-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("0", r.cedula!![0])
        assertEquals("PE", r.cedula[1])
        assertEquals("1234", r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    @Test fun `E-8-102017 completa valida`() {
        val r = CedulaHelper.validar("E-8-102017")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("0", r.cedula!![0])
        assertEquals("E", r.cedula[1])
        assertEquals("8", r.cedula[2])
        assertEquals("102017", r.cedula[3])
    }

    @Test fun `N-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("N-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("0", r.cedula!![0])
        assertEquals("N", r.cedula[1])
        assertEquals("1234", r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    @Test fun `E-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("E-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("0", r.cedula!![0])
        assertEquals("E", r.cedula[1])
        assertEquals("1234",  r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    @Test fun `1PI-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("1PI-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("1", r.cedula!![0])
        assertEquals("PI", r.cedula[1])
        assertEquals("1234", r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    @Test fun `10AV-1234-12345 completa valida`() {
        val r = CedulaHelper.validar("10AV-1234-12345")
        assertTrue(r.esValida)
        assertTrue(r.esCompleta)
        assertEquals("10", r.cedula!![0])
        assertEquals("AV", r.cedula[1])
        assertEquals("1234", r.cedula[2])
        assertEquals("12345", r.cedula[3])
    }

    //  Cédulas inválidas 
    @Test fun `111 no es valida`() = assertFalse(CedulaHelper.validar("111").esValida)
    @Test fun `A no es valida`() = assertFalse(CedulaHelper.validar("A").esValida)
    @Test fun `P- no es valida`() = assertFalse(CedulaHelper.validar("P-").esValida)
    @Test fun `14- no es valida`() = assertFalse(CedulaHelper.validar("14-").esValida)
    @Test fun `13-12345 no es valida`() = assertFalse(CedulaHelper.validar("13-12345").esValida)
    @Test fun `12-1234-1234567 no es valida`() = assertFalse(CedulaHelper.validar("12-1234-1234567").esValida)

    //  Válida mientras escribe 
    @Test fun `valida escribiendo 1-12-123`() = esValidaMientrasEscribe("1-12-123")
    @Test fun `valida escribiendo 8-12-123`() = esValidaMientrasEscribe("8-12-123")
    @Test fun `valida escribiendo 12-12-12345`() = esValidaMientrasEscribe("12-12-12345")
    @Test fun `valida escribiendo PE-12-12345`() = esValidaMientrasEscribe("PE-12-12345")
    @Test fun `valida escribiendo N-12-123`() = esValidaMientrasEscribe("N-12-123")
    @Test fun `valida escribiendo E-12-123`() = esValidaMientrasEscribe("E-12-123")
    @Test fun `valida escribiendo 1PI-12-123`() = esValidaMientrasEscribe("1PI-12-123")
    @Test fun `valida escribiendo 13AV-12-123`() = esValidaMientrasEscribe("13AV-12-123")

    //  Incompleta mientras escribe 
    @Test fun `incompleta escribiendo 1-12-`() = noEsCompletaMientrasEscribe("1-12-")
    @Test fun `incompleta escribiendo 8-12-`() = noEsCompletaMientrasEscribe("8-12-")
    @Test fun `incompleta escribiendo 12-12-`() = noEsCompletaMientrasEscribe("12-12-")
    @Test fun `incompleta escribiendo PE-12-`() = noEsCompletaMientrasEscribe("PE-12-")
    @Test fun `incompleta escribiendo N-12-`() = noEsCompletaMientrasEscribe("N-12-")
    @Test fun `incompleta escribiendo E-1234-`() = noEsCompletaMientrasEscribe("E-1234-")
    @Test fun `incompleta escribiendo 1PI-1234-`()= noEsCompletaMientrasEscribe("1PI-1234-")
    @Test fun `incompleta escribiendo 13AV-12-`() = noEsCompletaMientrasEscribe("13AV-12-")
}