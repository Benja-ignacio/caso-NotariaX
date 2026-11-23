package com.duoc.notix.viewmodel

import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.testutil.MainDispatcherRule
import com.duoc.notix.testutil.usuario
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
class PerfilViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<TramiteRepository>(relaxed = true)

    @Test
    fun exponeUsuarioDesdeRoom() = runTest {
        every { repo.observarUsuario("USR-001") } returns flowOf(usuario())
        val vm = PerfilViewModel(repo, "USR-001")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.usuario.collect { } }
        runCurrent()

        assertEquals("Ana Demo Prueba", vm.usuario.value?.nombreCompleto)
        assertEquals("ana.demo@notariax.test", vm.usuario.value?.correo)
    }

    @Test
    fun exponeNullSiNoEncuentraUsuario() = runTest {
        every { repo.observarUsuario("USR-404") } returns flowOf(null)
        val vm = PerfilViewModel(repo, "USR-404")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.usuario.collect { } }
        runCurrent()

        assertNull(vm.usuario.value)
    }
}
