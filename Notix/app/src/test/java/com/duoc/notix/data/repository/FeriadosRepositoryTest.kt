package com.duoc.notix.data.repository

import com.duoc.notix.data.remote.api.FeriadosApi
import com.duoc.notix.data.remote.dto.FeriadoDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeriadosRepositoryTest {

    @Test
    fun usaElPrimerFeriadoYFormateaLaFecha() = runBlocking {
        val api = object : FeriadosApi {
            override suspend fun proximos(pais: String) = listOf(
                FeriadoDto(
                    date = "2026-12-08",
                    localName = "Inmaculada Concepción",
                    name = "Immaculate Conception"
                )
            )
        }

        val feriado = FeriadosRepository(api).obtenerProximoFeriado()

        assertEquals("08/12/2026", feriado?.fecha)
        assertEquals("Inmaculada Concepción", feriado?.nombre)
    }

    @Test
    fun devuelveNullCuandoLaApiFalla() = runBlocking {
        val api = object : FeriadosApi {
            override suspend fun proximos(pais: String): List<FeriadoDto> {
                throw IOException("sin red")
            }
        }

        assertNull(FeriadosRepository(api).obtenerProximoFeriado())
    }

    @Test
    fun devuelveNullCuandoNoHayFeriados() = runBlocking {
        val api = object : FeriadosApi {
            override suspend fun proximos(pais: String) = emptyList<FeriadoDto>()
        }

        assertNull(FeriadosRepository(api).obtenerProximoFeriado())
    }
}
