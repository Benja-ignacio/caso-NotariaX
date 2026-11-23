package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.repository.AuthRepository
import com.duoc.notix.data.repository.AuthResult
import com.duoc.notix.testutil.MainDispatcherRule
import com.duoc.notix.testutil.usuario
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<AuthRepository>()

    @Test
    fun loginExitosoNotificaUsuario() = runTest {
        coEvery { repo.login(TipoCuenta.PERSONA, "ana@test.cl", "demo123") } returns
            AuthResult.Success(usuario())
        val vm = LoginViewModel(repo)
        var recibido = false

        vm.ingresar(TipoCuenta.PERSONA, "ana@test.cl", "demo123") { recibido = true }
        runCurrent()

        assertTrue(recibido)
        assertNull(vm.error)
    }

    @Test
    fun loginFallidoExponeError() = runTest {
        coEvery { repo.login(any(), any(), any()) } returns
            AuthResult.Error("Contraseña incorrecta.")
        val vm = LoginViewModel(repo)
        var recibido = false

        vm.ingresar(TipoCuenta.PERSONA, "ana@test.cl", "mala") { recibido = true }
        runCurrent()

        assertFalse(recibido)
        assertEquals("Contraseña incorrecta.", vm.error)
    }
}
