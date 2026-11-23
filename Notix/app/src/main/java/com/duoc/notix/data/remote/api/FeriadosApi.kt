package com.duoc.notix.data.remote.api

import com.duoc.notix.data.remote.dto.FeriadoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FeriadosApi {

    @GET("api/v3/NextPublicHolidays/{pais}")
    suspend fun proximos(@Path("pais") pais: String): List<FeriadoDto>
}
