package com.duoc.notix.data.repository

import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.remote.api.UsuariosApi
import com.duoc.notix.data.remote.dto.LoginRequestDto
import com.duoc.notix.data.remote.dto.RegistroUsuarioRequestDto
import com.duoc.notix.testutil.usuario
import com.duoc.notix.testutil.usuarioDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthRepositoryTest {

    private val api = mockk<UsuariosApi>()
    private val dao = mockk<UsuarioDao>(relaxed = true)
    private val repo = AuthRepository(dao, api)

    @Test
    fun loginRemotoGuardaUsuarioEnRoom() = runTest {
        coEvery { api.login(any()) } returns usuarioDto()
        coEvery { dao.upsertUsuario(any()) } returns Unit

        val result = repo.login(TipoCuenta.PERSONA, "  ANA.DEMO@notariax.test ", "demo123")

        assertTrue(result is AuthResult.Success)
        val usuario = (result as AuthResult.Success).usuario
        assertEquals("USR-001", usuario.id)
        assertEquals("demo123", usuario.contrasena)
        coVerify { dao.upsertUsuario(match { it.id == "USR-001" && it.contrasena == "demo123" }) }
    }

    @Test
    fun loginUsaMensajeDelBackendEnErrorHttp() = runTest {
        coEvery { api.login(any()) } throws httpError(
            401,
            """{"status":401,"message":"La contraseña es incorrecta."}"""
        )

        val result = repo.login(TipoCuenta.PERSONA, "ana.demo@notariax.test", "mala")

        assertEquals(AuthResult.Error("La contraseña es incorrecta."), result)
    }

    @Test
    fun loginSinRedUsaCopiaLocal() = runTest {
        coEvery { api.login(any()) } throws IOException("sin red")
        coEvery { dao.getUsuarioPorCorreo("ana.demo@notariax.test") } returns
            usuario().toEntity()

        val result = repo.login(TipoCuenta.PERSONA, "ana.demo@notariax.test", "demo123")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun loginSinRedRechazaUsuarioInexistenteClaveOTipo() = runTest {
        coEvery { api.login(any()) } throws IOException("sin red")
        coEvery { dao.getUsuarioPorCorreo("nadie@test.cl") } returns null

        assertEquals(
            AuthResult.Error("Sin conexión con el servidor y no hay una cuenta local guardada."),
            repo.login(TipoCuenta.PERSONA, "nadie@test.cl", "demo123")
        )

        coEvery { dao.getUsuarioPorCorreo("ana.demo@notariax.test") } returns usuario().toEntity()
        assertEquals(
            AuthResult.Error("Contraseña incorrecta."),
            repo.login(TipoCuenta.PERSONA, "ana.demo@notariax.test", "mala")
        )
        assertEquals(
            AuthResult.Error("Esa cuenta no es de tipo Notario."),
            repo.login(TipoCuenta.NOTARIO, "ana.demo@notariax.test", "demo123")
        )
    }

    @Test
    fun registroRemotoNormalizaCorreoYGuardaUsuario() = runTest {
        val request = slot<RegistroUsuarioRequestDto>()
        coEvery { api.registrar(capture(request)) } returns usuarioDto()
        coEvery { dao.upsertUsuario(any()) } returns Unit

        val result = repo.registrar(
            usuario(correo = "NUEVA.PERSONA@NOTARIAX.TEST", contrasena = "clave123")
        )

        assertTrue(result is AuthResult.Success)
        assertEquals("nueva.persona@notariax.test", request.captured.correo)
        assertEquals("clave123", request.captured.contrasena)
        coVerify { dao.upsertUsuario(any()) }
    }

    @Test
    fun registroReportaErrorHttpYFaltaDeRed() = runTest {
        coEvery { api.registrar(any()) } throws httpError(
            409,
            """{"message":"Ya existe una cuenta con ese correo."}"""
        )
        assertEquals(
            AuthResult.Error("Ya existe una cuenta con ese correo."),
            repo.registrar(usuario())
        )

        coEvery { api.registrar(any()) } throws IOException("sin red")
        assertEquals(
            AuthResult.Error("Sin conexión con el servidor. Verifica que los microservicios estén activos."),
            repo.registrar(usuario())
        )
    }

    @Test
    fun registroReportaExcepcionGenerica() = runTest {
        coEvery { api.registrar(any()) } throws IllegalStateException("fallo local")

        assertEquals(AuthResult.Error("fallo local"), repo.registrar(usuario()))
    }

    private fun httpError(code: Int, json: String) = HttpException(
        Response.error<Unit>(
            code,
            json.toResponseBody("application/json".toMediaType())
        )
    )
}
