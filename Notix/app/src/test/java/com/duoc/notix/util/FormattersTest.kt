package com.duoc.notix.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun limpiaYFormateaRut() {
        assertEquals("21863636", limpiarRut("21.863.636"))
        assertEquals("21863636K", limpiarRut("21.863.636-k"))
        assertEquals("21863636K", limpiarRut("21863636k"))
        assertEquals("21.863.636-6", formatearRut("218636366"))
        assertEquals("21.863.636-K", formatearRut("21863636K"))
    }

    @Test
    fun limpiarRutIgnoraCaracteresInvalidosYExceso() {
        assertEquals("123456789", limpiarRut("12a34-56 7890"))
        assertEquals("12345678K", limpiarRut("12345678k"))
        assertEquals("", limpiarRut("abc"))
    }

    @Test
    fun limpiaYFormateaDocumento() {
        assertEquals("123456789", limpiarDocumento("12.345.678-9"))
        assertEquals("123456789", limpiarDocumento("1234567890"))
        assertEquals("123.456.789", formatearDocumento("123456789"))
        assertEquals("", formatearDocumento("abc"))
    }

    @Test
    fun formateaFechaCorta() {
        assertEquals("12 oct 2026", fechaCorta("12/10/2026 10:00"))
        assertEquals("", fechaCorta(null))
        assertEquals("texto", fechaCorta("texto"))
        assertEquals("12/13/2026", fechaCorta("12/13/2026"))
    }
}
