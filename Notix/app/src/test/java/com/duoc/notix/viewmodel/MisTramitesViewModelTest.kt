package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MisTramitesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<TramiteRepository>(relaxed = true)

    private fun mockFlows() {
        every { repo.observarUsuario("USR-001") } returns flowOf(usuario())
        every { repo.observarMisTramites("USR-001") } returns flowOf(
            listOf(
                tramite(id = "TX-2026-0001", estado = EstadoTramite.ENVIADO),
                tramite(id = "TX-2026-0042", estado = EstadoTramite.DEVUELTO),
                tramite(id = "TX-2026-0031", estado = EstadoTramite.FINALIZADO)
            )
        )
        every { repo.observarAvisosSinLeer("USR-001") } returns flowOf(true)
        coEvery { repo.refrescarMisTramites(any()) } returns Unit
        coEvery { repo.refrescarAvisos(any()) } returns emptyList()
    }

    @Test
    fun construyeEstadoConFiltrosYContadores() = runTest {
        mockFlows()
        val vm = MisTramitesViewModel(repo, "USR-001")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        runCurrent()

        val todos = vm.uiState.value
        assertEquals("Persona natural", todos.perfil)
        assertEquals("Ana", todos.nombre)
        assertEquals(3, todos.tramites.size)
        assertEquals(1, todos.enRevision)
        assertEquals(1, todos.observados)
        assertEquals(1, todos.finalizados)
        assertEquals("TX-2026-0042", todos.alerta?.id)
        assertTrue(todos.avisosSinLeer)

        vm.onFiltro(FiltroTramites.OBSERVADOS)
        runCurrent()
        assertEquals(FiltroTramites.OBSERVADOS, vm.uiState.value.filtro)
        assertEquals(listOf("TX-2026-0042"), vm.uiState.value.tramites.map { it.id })

        vm.onFiltro(FiltroTramites.FINALIZADOS)
        runCurrent()
        assertEquals(listOf("TX-2026-0031"), vm.uiState.value.tramites.map { it.id })

        vm.onFiltro(FiltroTramites.EN_CURSO)
        runCurrent()
        assertEquals(2, vm.uiState.value.tramites.size)
    }

    @Test
    fun validaReenvioAntesDeLlamarAlRepositorio() = runTest {
        mockFlows()
        val vm = MisTramitesViewModel(repo, "USR-001")
        val valido = Antecedentes("Ana", "111111111", "ana@test.cl", "Ñuñoa")

        vm.reenviarTramite(
            tramite(id = "TX-2026-0001", estado = EstadoTramite.ENVIADO),
            valido,
            null,
            "",
            {}
        )
        assertEquals("Solo se pueden corregir trámites devueltos.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            valido.copy(nombreCompleto = " "),
            null,
            "",
            {}
        )
        assertEquals("Ingresa el nombre completo.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            valido.copy(documentoIdentidad = "111111112"),
            null,
            "",
            {}
        )
        assertEquals("El dígito verificador del RUN no coincide.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            valido.copy(correoContacto = "sin-arroba"),
            null,
            "",
            {}
        )
        assertEquals("Ingresa un correo válido.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            valido.copy(comuna = " "),
            null,
            "",
            {}
        )
        assertEquals("Ingresa la comuna.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        vm.reenviarTramite(
            tramite(
                estado = EstadoTramite.DEVUELTO,
                documentos = listOf(Documento("anexo.jpg", 100, EstadoDocumento.OBSERVADO))
            ),
            valido,
            null,
            "",
            {}
        )
        assertEquals("Adjunta un nuevo documento para reemplazar el observado.", vm.errorOperacion)
    }

    @Test
    fun reenviaCorrectamenteYNotificaExito() = runTest {
        mockFlows()
        coEvery { repo.reenviarTramite(any(), any(), any(), any()) } returns Unit
        val vm = MisTramitesViewModel(repo, "USR-001")
        var exito = false

        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            Antecedentes("Ana", "111111111", "ana@test.cl", "Ñuñoa"),
            "nuevo.pdf",
            "corregido"
        ) { exito = true }
        runCurrent()

        assertTrue(exito)
        assertNull(vm.errorOperacion)
        assertFalse(vm.operacionEnCurso)
    }

    @Test
    fun eliminaTramiteYNotificaExito() = runTest {
        mockFlows()
        coEvery { repo.eliminarTramite(any()) } returns Unit
        val vm = MisTramitesViewModel(repo, "USR-001")
        var exito = false

        vm.eliminarTramite(tramite()) { exito = true }
        runCurrent()

        assertTrue(exito)
        assertNull(vm.errorOperacion)
        assertFalse(vm.operacionEnCurso)
    }

    @Test
    fun noEliminaFinalizadoYMuestraErrorDelBackend() = runTest {
        mockFlows()
        val vm = MisTramitesViewModel(repo, "USR-001")

        vm.eliminarTramite(tramite(estado = EstadoTramite.FINALIZADO)) {}
        assertEquals("No se puede eliminar un trámite finalizado.", vm.errorOperacion)

        vm.limpiarErrorOperacion()
        coEvery { repo.eliminarTramite(any()) } throws IllegalStateException("backend caído")
        vm.eliminarTramite(tramite()) {}
        runCurrent()
        assertEquals("backend caído", vm.errorOperacion)
    }

    @Test
    fun muestraErrorSiElReenvioFalla() = runTest {
        mockFlows()
        coEvery { repo.reenviarTramite(any(), any(), any(), any()) } throws
            IllegalStateException("backend caído")
        val vm = MisTramitesViewModel(repo, "USR-001")

        vm.reenviarTramite(
            tramite(estado = EstadoTramite.DEVUELTO),
            Antecedentes("Ana", "111111111", "ana@test.cl", "Ñuñoa"),
            null,
            ""
        ) {}
        runCurrent()

        assertEquals("backend caído", vm.errorOperacion)
        assertFalse(vm.operacionEnCurso)
    }
}
