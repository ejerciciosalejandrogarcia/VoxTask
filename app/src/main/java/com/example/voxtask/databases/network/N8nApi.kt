package com.example.voxtask.databases.network

import com.example.voxtask.databases.model.Correo
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface N8nApiService {
    @GET("webhook/get-emails")
    suspend fun obtenerCorreos(
        @Query("token") token: String
    ): List<Correo>
}

object N8nClient {
    private const val BASE_URL = "http://192.168.1.43:5678/"

    val api: N8nApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(N8nApiService::class.java)
    }
}