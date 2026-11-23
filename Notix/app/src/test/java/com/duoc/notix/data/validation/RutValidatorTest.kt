package com.duoc.notix.data.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RutValidatorTest {

    @Test
    fun aceptaRunValido() {
        assertNull(RutValidator.validar("111111111"))
        assertNull(RutValidator.validar("222222222"))
    }

    @Test
    fun calculaDigitoVerificadorKYCero() {
        assertEquals('1', RutValidator.calcularDv("11111111"))
        assertEquals('5', RutValidator.calcularDv("12345678"))
        assertEquals('K', RutValidator.calcularDv("1000005"))
        assertEquals('0', RutValidator.calcularDv("14"))
    }

    @Test
    fun rechazaRunVacioOCorto() {
        assertEquals("Ingresa el RUN.", RutValidator.validar("   "))
        assertEquals("El RUN está incompleto.", RutValidator.validar("1234"))
    }

    @Test
    fun rechazaCuerpoConLetras() {
        assertEquals(
            "El RUN solo puede tener números y una K al final.",
            RutValidator.validar("12A456789")
        )
    }

    @Test
    fun rechazaDigitoVerificadorIncorrecto() {
        assertEquals(
            "El dígito verificador del RUN no coincide.",
            RutValidator.validar("111111112")
        )
    }

    @Test
    fun usaEtiquetaPersonalizada() {
        assertEquals("Ingresa el RUT.", RutValidator.validar("", "RUT"))
        assertEquals("El RUT está incompleto.", RutValidator.validar("1", "RUT"))
    }
}
