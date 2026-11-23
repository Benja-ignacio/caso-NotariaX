package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.testutil.MainDispatcherRule
import com.duoc.notix.testutil.tramite
import com.duoc.notix.testutil.usuario
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BandejaNotarioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<TramiteRepository>(relaxed = true)

    private fun mockFlows() {
        every { repo.observarUsuario("USR-NOTARIO-01") } returns flowOf(
            usuario(
                id = "USR-NOTARIO-01",
                tipo = TipoCuenta.NOTARIO,
                nombre = "Carlos Notario Silva",
                numeroNotaria = 2
            )
        )
        every { repo.observarTodosLosTramites() } returns flowOf(
            listOf(
                tramite(id = "TX-2026-0001", estado = EstadoTramite.ENVIADO),
                tramite(id = "TX-2026-0042", estado = EstadoTramite.DEVUELTO),
                tramite(id = "TX-2026-0031", estado = EstadoTramite.FINALIZADO)
            )
        )
        coEvery { repo.refrescarTodosLosTramites() } returns Unit
    }

    @Test
    fun construyeEstadoYFiltraLaBandeja() = runTest {
        mockFlows()
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        runCurrent()

        val todos = vm.uiState.value
        assertEquals("Carlos Notario Silva", todos.nombreNotario)
        assertEquals(2, todos.numeroNotaria)
        assertEquals(1, todos.enRevision)
        assertEquals(1, todos.observados)
        assertEquals(1, todos.finalizados)

        vm.onFiltro(FiltroTramites.EN_CURSO)
        runCurrent()
        assertEquals(listOf("TX-2026-0001"), vm.uiState.value.tramites.map { it.id })
    }

    @Test
    fun validaAprobacionYLegibilidad() = runTest {
        mockFlows()
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")

        vm.aprobarTramite(tramite(), false)
        assertEquals("Debes confirmar que los documentos son legibles antes de aprobar.", vm.errorOperacion)

        vm.limpiarError()
        vm.aprobarTramite(tramite(estado = EstadoTramite.DEVUELTO), true)
        assertEquals("Solo se pueden aprobar trámites en estado ENVIADO.", vm.errorOperacion)
    }

    @Test
    fun apruebaConLegibilidad() = runTest {
        mockFlows()
        coEvery { repo.actualizarEstadoTramite(any(), any(), any(), any(), any(), any()) } returns Unit
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")

        vm.aprobarTramite(tramite(), true)
        runCurrent()

        assertNull(vm.errorOperacion)
    }

    @Test
    fun validaDevolucionYObservacion() = runTest {
        mockFlows()
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")

        vm.devolverTramite(tramite(estado = EstadoTramite.DEVUELTO), "obs")
        assertEquals("Solo se pueden devolver trámites en estado ENVIADO.", vm.errorOperacion)

        vm.limpiarError()
        vm.devolverTramite(tramite(), "   ")
        assertEquals("Ingresa el motivo de devolución.", vm.errorOperacion)
    }

    @Test
    fun devuelveCorrectamente() = runTest {
        mockFlows()
        coEvery { repo.actualizarEstadoTramite(any(), any(), any(), any(), any(), any()) } returns Unit
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")

        vm.devolverTramite(tramite(), "  Falta un anexo  ")
        runCurrent()

        assertNull(vm.errorOperacion)
    }

    @Test
    fun muestraErrorDelRepositorio() = runTest {
        mockFlows()
        coEvery { repo.actualizarEstadoTramite(any(), any(), any(), any(), any(), any()) } throws
            IllegalStateException("Error del backend")
        val vm = BandejaNotarioViewModel(repo, "USR-NOTARIO-01")

        vm.aprobarTramite(tramite(), true)
        runCurrent()

        assertEquals("Error del backend", vm.errorOperacion)
        vm.limpiarError()
        assertNull(vm.errorOperacion)
    }
}
