package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.repository.AuthRepository
import com.duoc.notix.data.repository.AuthResult
import com.duoc.notix.testutil.MainDispatcherRule
import com.duoc.notix.testutil.usuario
import io.mockk.coEvery
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<AuthRepository>()

    @Test
    fun registraPersonaConLosCamposDelFormulario() = runTest {
        val capturado = slot<Usuario>()
        coEvery { repo.registrar(capture(capturado)) } returns AuthResult.Success(usuario())
        val vm = AuthViewModel(repo)
        var exito = false

        vm.registrar(
            TipoCuenta.PERSONA,
            mapOf(
                "nombre" to " Ana ",
                "correo" to "ana@test.cl",
                "rut" to "111111111",
                "doc" to "123456789",
                "telefono" to "912345678",
                "comuna" to "Ñuñoa"
            ),
            "demo123"
        ) { exito = true }
        runCurrent()

        assertTrue(exito)
        assertNull(vm.errorRegistro)
        assertEquals("Ana", capturado.captured.nombreCompleto)
        assertEquals(TipoCuenta.PERSONA, capturado.captured.tipoCuenta)
        assertEquals("demo123", capturado.captured.contrasena)
    }

    @Test
    fun registraEmpresaConRepresentanteYDatosComerciales() = runTest {
        val capturado = slot<Usuario>()
        coEvery { repo.registrar(capture(capturado)) } returns AuthResult.Success(usuario())
        val vm = AuthViewModel(repo)

        vm.registrar(
            TipoCuenta.EMPRESA,
            mapOf(
                "rep_nombre" to "Ana Representante",
                "correo" to "empresa@test.cl",
                "rep_run" to "111111111",
                "rep_doc" to "123456789",
                "telefono" to "912345678",
                "comuna" to "Santiago",
                "razon" to "Notix SpA",
                "rut" to "76543210-1",
                "giro" to "Servicios"
            ),
            "demo123"
        ) {}
        runCurrent()

        assertEquals("Ana Representante", capturado.captured.nombreCompleto)
        assertEquals("Notix SpA", capturado.captured.empresa?.razonSocial)
        assertEquals("76543210-1", capturado.captured.empresa?.rutEmpresa)
    }

    @Test
    fun registraNotarioConNumeroDeNotaria() = runTest {
        val capturado = slot<Usuario>()
        coEvery { repo.registrar(capture(capturado)) } returns AuthResult.Success(usuario())
        val vm = AuthViewModel(repo)

        vm.registrar(
            TipoCuenta.NOTARIO,
            mapOf(
                "nombre" to "Carlos Notario",
                "correo" to "notario@test.cl",
                "run" to "222222222",
                "doc" to "987654321",
                "telefono" to "987654321",
                "notaria_comuna" to "Santiago",
                "notaria_n" to "7"
            ),
            "notario123"
        ) {}
        runCurrent()

        assertEquals(TipoCuenta.NOTARIO, capturado.captured.tipoCuenta)
        assertEquals(7, capturado.captured.numeroNotaria)
    }

    @Test
    fun exponeErrorDeRegistro() = runTest {
        coEvery { repo.registrar(any()) } returns AuthResult.Error("Correo duplicado.")
        val vm = AuthViewModel(repo)
        var exito = false

        vm.registrar(TipoCuenta.PERSONA, emptyMap(), "demo123") { exito = true }
        runCurrent()

        assertFalse(exito)
        assertEquals("Correo duplicado.", vm.errorRegistro)
    }
}
