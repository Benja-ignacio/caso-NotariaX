package com.duoc.notix.data.remote.api

import com.duoc.notix.data.remote.dto.AvisoDto
import com.duoc.notix.data.remote.dto.CambioEstadoRequestDto
import com.duoc.notix.data.remote.dto.CrearTramiteRequestDto
import com.duoc.notix.data.remote.dto.MarcarLeidoRequestDto
import com.duoc.notix.data.remote.dto.ReenvioRequestDto
import com.duoc.notix.data.remote.dto.TramiteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TramitesApi {

    @GET("api/tramites")
    suspend fun todos(): List<TramiteDto>

    @GET("api/tramites/solicitante/{solicitanteId}")
    suspend fun porSolicitante(@Path("solicitanteId") solicitanteId: String): List<TramiteDto>

    @GET("api/tramites/{id}")
    suspend fun detalle(@Path("id") id: String): TramiteDto

    @POST("api/tramites")
    suspend fun crear(@Body request: CrearTramiteRequestDto): TramiteDto

    @PUT("api/tramites/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: String,
        @Body request: CambioEstadoRequestDto
    ): TramiteDto

    @PUT("api/tramites/{id}/reenvio")
    suspend fun reenviar(
        @Path("id") id: String,
        @Body request: ReenvioRequestDto
    ): TramiteDto

    @DELETE("api/tramites/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>

    @GET("api/avisos/{usuarioId}")
    suspend fun avisos(@Path("usuarioId") usuarioId: String): List<AvisoDto>

    @PUT("api/avisos/{id}/leido")
    suspend fun marcarLeido(
        @Path("id") id: String,
        @Body request: MarcarLeidoRequestDto
    ): AvisoDto
}
