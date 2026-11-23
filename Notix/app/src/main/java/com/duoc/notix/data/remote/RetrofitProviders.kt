package com.duoc.notix.data.remote

import com.duoc.notix.data.remote.api.FeriadosApi
import com.duoc.notix.data.remote.api.TramitesApi
import com.duoc.notix.data.remote.api.UsuariosApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private object RetrofitFactory {

    fun create(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object UsuariosApiProvider {
    val api: UsuariosApi by lazy {
        RetrofitFactory.create(ApiConfig.USUARIOS_BASE_URL).create(UsuariosApi::class.java)
    }
}

object TramitesApiProvider {
    val api: TramitesApi by lazy {
        RetrofitFactory.create(ApiConfig.TRAMITES_BASE_URL).create(TramitesApi::class.java)
    }
}

object FeriadosApiProvider {
    val api: FeriadosApi by lazy {
        RetrofitFactory.create(ApiConfig.FERIADOS_BASE_URL).create(FeriadosApi::class.java)
    }
}
