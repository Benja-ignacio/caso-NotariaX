package com.duoc.notix.viewmodel

import com.duoc.notix.data.model.Aviso
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvisosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<TramiteRepository>(relaxed = true)

    @Test
    fun observaAvisosYRefrescaAlIniciar() = runTest {
        every { repo.observarAvisos("USR-001") } returns flowOf(
            listOf(
                Aviso(
                    id = "AVS-1",
                    destinatarioId = "USR-001",
                    tramiteId = "TX-2026-0001",
                    mensaje = "Mensaje",
                    fechaHora = "12/10/2026 10:00"
                )
            )
        )
        coEvery { repo.refrescarAvisos("USR-001") } returns emptyList()
        val vm = AvisosViewModel(repo, "USR-001")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.avisos.collect { } }
        runCurrent()

        assertEquals("AVS-1", vm.avisos.value.single().id)
        coVerify { repo.refrescarAvisos("USR-001") }
    }

    @Test
    fun marcaAvisoComoLeido() = runTest {
        every { repo.observarAvisos("USR-001") } returns flowOf(emptyList())
        coEvery { repo.refrescarAvisos("USR-001") } returns emptyList()
        coEvery { repo.marcarAvisoLeido("AVS-1") } returns Unit
        val vm = AvisosViewModel(repo, "USR-001")

        vm.marcarComoLeido("AVS-1")
        runCurrent()

        coVerify { repo.marcarAvisoLeido("AVS-1") }
    }
}
