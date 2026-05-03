package com.example.voxtask.databases.network

import com.example.voxtask.databases.model.Correo
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class EnviarCorreoRequest(
    val token: String,
    val para: String,
    val asunto: String,
    val mensaje: String,
    val modo: String
)

data class VerificacionRequest(
    val email: String,
    val uid: String
)


data class RecuperarContraseniaRequest(
    val email: String
)
data class VerificacionResponse(
    val success: Boolean,
    val codigo: String
)

interface N8nApiService {
    @GET("webhook/get-emails")
    suspend fun obtenerCorreos(
        @Query("token") token: String
    ): List<Correo>

    @POST("webhook/send-email")
    suspend fun enviarCorreo(
        @Body body: EnviarCorreoRequest
    ): retrofit2.Response<Unit>


    @GET("webhook/efbd4541-edb4-4dfd-bfcc-0800ad0253f9/correo/{id}")
    suspend fun obtenerCorreoPorId(
        @Path("id") id: String,
        @Query("token") token: String
    ): Correo

    @POST("webhook/send-verification")
    suspend fun enviarCodigoVerificacion(
        @Body body: VerificacionRequest
    ): VerificacionResponse

    @POST("webhook/recuperar-contrasenia")
    suspend fun enviarCorreoRecuperacion(
        @Body body: RecuperarContraseniaRequest
    ): retrofit2.Response<Unit>
}

object N8nClient {
    private const val BASE_URL = "http://192.168.1.42:5678/"

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