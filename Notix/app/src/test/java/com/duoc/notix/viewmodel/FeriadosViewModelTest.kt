package com.duoc.notix.viewmodel

import com.duoc.notix.data.repository.Feriado
import com.duoc.notix.data.repository.FeriadosRepository
import com.duoc.notix.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeriadosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<FeriadosRepository>()

    @Test
    fun cargaProximoFeriado() = runTest {
        coEvery { repo.obtenerProximoFeriado() } returns
            Feriado("08/12/2026", "Inmaculada Concepción")
        val vm = FeriadosViewModel(repo)
        runCurrent()

        val estado = vm.uiState
        assertFalse(estado.cargando)
        assertFalse(estado.sinDatos)
        assertEquals("Inmaculada Concepción", estado.nombre)
        assertEquals("08/12/2026", estado.fecha)
    }

    @Test
    fun muestraFallbackSiNoHayFeriado() = runTest {
        coEvery { repo.obtenerProximoFeriado() } returns null
        val vm = FeriadosViewModel(repo)
        runCurrent()

        val estado = vm.uiState
        assertFalse(estado.cargando)
        assertTrue(estado.sinDatos)
        assertNull(estado.nombre)
    }

    @Test
    fun permiteReintentarLaCarga() = runTest {
        coEvery { repo.obtenerProximoFeriado() } returns null
        val vm = FeriadosViewModel(repo)
        runCurrent()
        assertTrue(vm.uiState.sinDatos)

        coEvery { repo.obtenerProximoFeriado() } returns Feriado("25/12/2026", "Navidad")
        vm.cargar()
        runCurrent()

        assertEquals("Navidad", vm.uiState.nombre)
        assertFalse(vm.uiState.sinDatos)
    }
}
