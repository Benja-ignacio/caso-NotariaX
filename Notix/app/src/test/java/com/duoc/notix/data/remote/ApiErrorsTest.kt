package com.duoc.notix.data.remote

import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiErrorsTest {

    @Test
    fun traduceErrorDeRed() {
        assertEquals(
            "Sin conexión con el servidor. Se mantiene la última información guardada.",
            ApiErrors.mensaje(IOException("timeout"))
        )
    }

    @Test
    fun leeMensajeDelBackendEnHttpException() {
        val body = """{"status":409,"message":"El trámite ya está finalizado."}"""
            .toResponseBody("application/json".toMediaType())
        val error = HttpException(Response.error<Unit>(409, body))

        assertEquals("El trámite ya está finalizado.", ApiErrors.mensaje(error))
    }

    @Test
    fun usaMensajeGenericoSiElErrorNoTraeJson() {
        val body = "<html>error</html>".toResponseBody("text/html".toMediaType())
        val error = HttpException(Response.error<Unit>(500, body))

        assertTrue(ApiErrors.mensaje(error).contains("500"))
    }

    @Test
    fun usaMensajeDeExcepcionGenerica() {
        assertEquals("boom", ApiErrors.mensaje(IllegalStateException("boom")))
    }
}
