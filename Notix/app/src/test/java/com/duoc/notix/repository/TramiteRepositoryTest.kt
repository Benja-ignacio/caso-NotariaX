package com.duoc.notix.repository

import com.duoc.notix.data.local.AvisoDao
import com.duoc.notix.data.local.TramiteDao
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.remote.api.TramitesApi
import com.duoc.notix.data.remote.dto.AvisoDto
import com.duoc.notix.data.remote.dto.CambioEstadoRequestDto
import com.duoc.notix.data.remote.dto.CrearTramiteRequestDto
import com.duoc.notix.data.remote.dto.MarcarLeidoRequestDto
import com.duoc.notix.data.remote.dto.ReenvioRequestDto
import com.duoc.notix.testutil.tramite
import com.duoc.notix.testutil.tramiteDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TramiteRepositoryTest {

    private val tramiteDao = mockk<TramiteDao>(relaxed = true)
    private val avisoDao = mockk<AvisoDao>(relaxed = true)
    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val api = mockk<TramitesApi>()
    private val repo = TramiteRepository(tramiteDao, avisoDao, usuarioDao, api)

    @Test
    fun refrescaMisTramitesYTodosLosTramites() = runTest {
        coEvery { api.porSolicitante("USR-001") } returns listOf(tramiteDto())
        coEvery { api.todos() } returns listOf(tramiteDto(id = "TX-2026-0001"))
        coEvery { tramiteDao.upsertTramiteRemoto(any(), any(), any()) } returns Unit

        repo.refrescarMisTramites("USR-001")
        repo.refrescarTodosLosTramites()

        coVerify { tramiteDao.upsertTramiteRemoto(match { it.id == "TX-2026-0042" }, any(), any()) }
        coVerify { tramiteDao.upsertTramiteRemoto(match { it.id == "TX-2026-0001" }, any(), any()) }
    }

    @Test
    fun refrescaAvisosYLosMarcaComoLeidos() = runTest {
        coEvery { api.avisos("USR-001") } returns listOf(
            AvisoDto(
                id = "AVS-1",
                destinatarioId = "USR-001",
                tramiteId = "TX-2026-0001",
                mensaje = "Mensaje",
                fechaHora = "2026-10-13T09:30:00"
            )
        )
        coEvery { api.marcarLeido("AVS-1", any()) } returns AvisoDto(id = "AVS-1")
        coEvery { avisoDao.insertAviso(any()) } returns Unit
        coEvery { avisoDao.marcarAvisoLeido("AVS-1") } returns Unit

        repo.refrescarAvisos("USR-001")
        repo.marcarAvisoLeido("AVS-1")

        coVerify { avisoDao.insertAviso(match { it.id == "AVS-1" }) }
        coVerify { avisoDao.marcarAvisoLeido("AVS-1") }
        coVerify { api.marcarLeido("AVS-1", any<MarcarLeidoRequestDto>()) }
    }

    @Test
    fun marcarAvisoMantieneCambioLocalSiFallaLaRed() = runTest {
        coEvery { api.marcarLeido("AVS-1", any()) } throws IOException("sin red")
        coEvery { avisoDao.marcarAvisoLeido("AVS-1") } returns Unit

        repo.marcarAvisoLeido("AVS-1")

        coVerify { avisoDao.marcarAvisoLeido("AVS-1") }
    }

    @Test
    fun crearTramiteUsaBackendYGuardaRespuesta() = runTest {
        val request = slot<CrearTramiteRequestDto>()
        coEvery { api.crear(capture(request)) } returns tramiteDto(id = "TX-2026-0050")
        coEvery { tramiteDao.upsertTramiteRemoto(any(), any(), any()) } returns Unit

        val creado = repo.crearTramite(tramite())

        assertEquals("TX-2026-0050", creado.id)
        assertEquals("SIMPLE", request.captured.tipoFirma)
        assertEquals("SIMPLE_CANVAS", request.captured.firmaEvidencia)
        coVerify { tramiteDao.upsertTramiteRemoto(match { it.id == "TX-2026-0050" }, any(), any()) }
    }

    @Test
    fun cambiaEstadoUsandoBackend() = runTest {
        val request = slot<CambioEstadoRequestDto>()
        coEvery { api.cambiarEstado("TX-2026-0001", capture(request)) } returns
            tramiteDto(id = "TX-2026-0001", estado = "FINALIZADO", observacion = null)
                .copy(legibilidadConfirmada = true)
        coEvery { tramiteDao.upsertTramiteRemoto(any(), any(), any()) } returns Unit

        repo.actualizarEstadoTramite(
            tramite = tramite(),
            nuevoEstado = EstadoTramite.FINALIZADO,
            legibilidadConfirmada = true,
            rolHistorial = TipoCuenta.NOTARIO
        )

        assertEquals("FINALIZADO", request.captured.estado)
        assertEquals(true, request.captured.legibilidadConfirmada)
        assertEquals("NOTARIO", request.captured.rol)
        coVerify { tramiteDao.upsertTramiteRemoto(any(), any(), any()) }
    }

    @Test
    fun validaTransicionesAntesDeLlamarAlBackend() {
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.actualizarEstadoTramite(
                    tramite = tramite(estado = EstadoTramite.FINALIZADO),
                    nuevoEstado = EstadoTramite.FINALIZADO,
                    legibilidadConfirmada = true,
                    rolHistorial = TipoCuenta.NOTARIO
                )
            }
        }
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.actualizarEstadoTramite(
                    tramite = tramite(),
                    nuevoEstado = EstadoTramite.FINALIZADO,
                    legibilidadConfirmada = false,
                    rolHistorial = TipoCuenta.NOTARIO
                )
            }
        }
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.actualizarEstadoTramite(
                    tramite = tramite(),
                    nuevoEstado = EstadoTramite.DEVUELTO,
                    observacion = " ",
                    rolHistorial = TipoCuenta.NOTARIO
                )
            }
        }
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.actualizarEstadoTramite(
                    tramite = tramite(),
                    nuevoEstado = EstadoTramite.ENVIADO,
                    rolHistorial = TipoCuenta.PERSONA
                )
            }
        }
    }

    @Test
    fun reenviaTramiteObservadoYReemplazaDocumento() = runTest {
        val request = slot<ReenvioRequestDto>()
        val original = tramite(
            estado = EstadoTramite.DEVUELTO,
            documentos = listOf(
                Documento("aceptado.pdf", 100, EstadoDocumento.ACEPTADO),
                Documento("observado.jpg", 200, EstadoDocumento.OBSERVADO)
            )
        )
        coEvery { api.reenviar("TX-2026-0001", capture(request)) } returns
            tramiteDto(id = "TX-2026-0001", estado = "ENVIADO", observacion = null)
        coEvery { tramiteDao.upsertTramiteRemoto(any(), any(), any()) } returns Unit

        repo.reenviarTramite(
            tramite = original,
            antecedentes = original.antecedentes.copy(comuna = "Providencia"),
            nuevoDocumento = Documento("reemplazo.pdf", 300),
            comentario = "corregido"
        )

        assertEquals("Providencia", request.captured.antecedentes.comuna)
        assertEquals(listOf("aceptado.pdf", "reemplazo.pdf"), request.captured.documentos.map { it.nombre })
        assertEquals("corregido", request.captured.comentario)
        coVerify { tramiteDao.upsertTramiteRemoto(any(), any(), any()) }
    }

    @Test
    fun validaReenvio() {
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.reenviarTramite(
                    tramite = tramite(estado = EstadoTramite.ENVIADO),
                    antecedentes = tramite().antecedentes,
                    nuevoDocumento = null,
                    comentario = ""
                )
            }
        }

        val observado = tramite(
            estado = EstadoTramite.DEVUELTO,
            documentos = listOf(Documento("observado.jpg", 200, EstadoDocumento.OBSERVADO))
        )
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.reenviarTramite(
                    tramite = observado,
                    antecedentes = observado.antecedentes,
                    nuevoDocumento = null,
                    comentario = ""
                )
            }
        }
    }

    @Test
    fun eliminaTramiteEnBackendYRoom() = runTest {
        coEvery { api.eliminar("TX-2026-0001") } returns Response.success(Unit)
        coEvery { tramiteDao.eliminarTramiteLocal("TX-2026-0001") } returns Unit

        repo.eliminarTramite(tramite())

        coVerify { api.eliminar("TX-2026-0001") }
        coVerify { tramiteDao.eliminarTramiteLocal("TX-2026-0001") }
    }

    @Test
    fun noEliminaUnTramiteFinalizado() {
        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.eliminarTramite(tramite(estado = EstadoTramite.FINALIZADO))
            }
        }
        assertTrue(error.message!!.contains("finalizado"))
    }

    @Test
    fun propagaErrorDelBackendAlEliminar() = runTest {
        coEvery { api.eliminar("TX-2026-0001") } returns Response.error(
            409,
            """{"message":"No se puede eliminar un trámite finalizado."}"""
                .toResponseBody("application/json".toMediaType())
        )

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.eliminarTramite(tramite()) }
        }
        assertTrue(error.message!!.contains("No se puede eliminar"))
    }

    @Test
    fun traduceErrorDeRedAlRefrescar() {
        coEvery { api.todos() } throws IOException("sin red")

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.refrescarTodosLosTramites() }
        }
        assertTrue(error.message!!.contains("Sin conexión"))
    }
}
