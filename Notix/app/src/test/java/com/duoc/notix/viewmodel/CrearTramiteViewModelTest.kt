package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.testutil.MainDispatcherRule
import com.duoc.notix.testutil.formato
import com.duoc.notix.testutil.tramite
import com.duoc.notix.testutil.usuario
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
class CrearTramiteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<TramiteRepository>(relaxed = true)
    private val formato = formato(firma = TipoFirma.SIMPLE)

    private suspend fun crearVm(): CrearTramiteViewModel {
        coEvery { repo.obtenerUsuario("USR-001") } returns usuario()
        return CrearTramiteViewModel(repo, "USR-001", formato)
    }

    @Test
    fun cargaAntecedentesDelUsuario() = runTest {
        val vm = crearVm()
        runCurrent()

        assertEquals("Ana Demo Prueba", vm.nombreCompleto)
        assertEquals("111111111", vm.run)
        assertEquals("ana.demo@notariax.test", vm.correo)
        assertEquals("Ñuñoa", vm.comuna)
        assertTrue(vm.uiState.usuario != null)
        assertFalse(vm.uiState.cargandoUsuario)
    }

    @Test
    fun validaCamposDelFormulario() = runTest {
        val vm = crearVm()
        runCurrent()
        assertTrue(vm.validarFormulario())

        vm.onNombreChange(" ")
        assertEquals("El nombre no puede estar vacío", vm.nombreError)
        assertFalse(vm.validarFormulario())
        assertEquals("Ingresa el nombre completo", vm.nombreError)

        vm.onNombreChange("Ana")
        vm.onRunChange("111111112")
        assertFalse(vm.validarFormulario())
        assertEquals("El dígito verificador del RUN no coincide.", vm.runError)

        vm.onRunChange("111111111")
        vm.onCorreoChange("sin-arroba")
        assertFalse(vm.validarFormulario())
        assertEquals("Correo inválido", vm.correoError)

        vm.onCorreoChange("ana@test.cl")
        vm.onComunaChange(" ")
        assertFalse(vm.validarFormulario())
        assertEquals("Ingresa la comuna", vm.comunaError)
    }

    @Test
    fun exigeFirmaAntesDeGuardar() = runTest {
        val vm = crearVm()
        runCurrent()
        var exito = false

        vm.guardarTramite { exito = true }
        runCurrent()

        assertFalse(exito)
        assertFalse(vm.uiState.guardadoExitoso)
        assertEquals("Debes completar la firma simulada antes de enviar.", vm.uiState.errorGeneral)
    }

    @Test
    fun guardaTramiteConFirmaYDocumento() = runTest {
        val capturado = slot<Tramite>()
        coEvery { repo.crearTramite(capture(capturado)) } returns tramite(id = "TX-2026-0050")
        coEvery { repo.refrescarAvisos(any()) } returns Unit
        val vm = crearVm()
        runCurrent()
        vm.onFirmaConfirmada("SIMPLE_CANVAS")
        vm.onDocumentoAdjuntado("poder.pdf")
        var exito = false

        vm.guardarTramite { exito = true }
        runCurrent()

        assertTrue(exito)
        assertTrue(vm.uiState.guardadoExitoso)
        assertEquals("poder.pdf", capturado.captured.documentos.single().nombre)
        assertEquals("SIMPLE_CANVAS", capturado.captured.firmaEvidencia)
        assertEquals("", capturado.captured.id)
    }

    @Test
    fun reportaErrorAlGuardar() = runTest {
        coEvery { repo.crearTramite(any()) } throws IllegalStateException("sin red")
        coEvery { repo.obtenerUsuario("USR-001") } returns usuario()
        val vm = CrearTramiteViewModel(repo, "USR-001", formato)
        runCurrent()
        vm.onFirmaConfirmada("SIMPLE_CANVAS")

        vm.guardarTramite {}
        runCurrent()

        assertEquals("Error al guardar el trámite: sin red", vm.uiState.errorGeneral)
        assertFalse(vm.uiState.guardadoExitoso)
    }

    @Test
    fun noGuardaSiElFormularioEsInvalido() = runTest {
        val vm = crearVm()
        runCurrent()
        vm.onFirmaConfirmada("SIMPLE_CANVAS")
        vm.onNombreChange(" ")

        vm.guardarTramite {}
        runCurrent()

        assertNull(vm.uiState.errorGeneral)
        assertFalse(vm.uiState.guardadoExitoso)
    }

    @Test
    fun actualizaDocumentoYRepresentacion() = runTest {
        val vm = crearVm()
        runCurrent()

        vm.onDocumentoAdjuntado("documento.pdf")
        vm.onActuaEnNombreDeChange("Notix SpA")

        assertEquals("documento.pdf", vm.documentoAdjuntoNombre)
        assertEquals("Notix SpA", vm.actuaEnNombreDe)
    }

    @Test
    fun usaTipoDeFirmaDelFormato() = runTest {
        coEvery { repo.obtenerUsuario("USR-001") } returns usuario()
        val vm = CrearTramiteViewModel(repo, "USR-001", formato(firma = TipoFirma.NOTARIADA))
        runCurrent()

        assertEquals(TipoFirma.NOTARIADA, vm.formato.firmaRequerida)
        assertEquals(EstadoTramite.ENVIADO, tramite().estado)
    }
}
