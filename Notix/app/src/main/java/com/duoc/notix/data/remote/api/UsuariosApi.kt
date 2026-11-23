package com.duoc.notix.data.remote.api

import com.duoc.notix.data.remote.dto.LoginRequestDto
import com.duoc.notix.data.remote.dto.RegistroUsuarioRequestDto
import com.duoc.notix.data.remote.dto.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuariosApi {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body request: RegistroUsuarioRequestDto): UsuarioDto

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequestDto): UsuarioDto

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: String): UsuarioDto
}
