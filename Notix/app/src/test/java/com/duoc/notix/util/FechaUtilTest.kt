package com.duoc.notix.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FechaUtilTest {

    @Test
    fun convierteFechaIsoDelBackendAlFormatoVisual() {
        assertEquals("23/11/2026 15:11", fechaIsoAApp("2026-11-23T15:11:23.123456"))
    }

    @Test
    fun convierteFechaSinHoraDelApiDeFeriados() {
        assertEquals("08/12/2026", fechaDiaAApp("2026-12-08"))
    }

    @Test
    fun conservaTextoSiNoEsUnaFechaIso() {
        assertEquals("texto libre", fechaIsoAApp("texto libre"))
        assertEquals("texto libre", fechaDiaAApp("texto libre"))
    }

    @Test
    fun fechaVaciaSeMantieneVacia() {
        assertEquals("", fechaIsoAApp(null))
        assertEquals("", fechaIsoAApp(""))
    }
}
