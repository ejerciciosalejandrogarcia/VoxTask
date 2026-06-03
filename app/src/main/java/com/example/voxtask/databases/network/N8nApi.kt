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

/**
 * Cuerpo de la petición para enviar un correo a través del flujo de n8n
 */
data class EnviarCorreoRequest(
    val token: String,
    val para: String,
    val asunto: String,
    val mensaje: String,
    val modo: String,
    val idioma: String
)

/**
 * Respuesta del webhook de clima con los datos meteorológicos de la ubicación del usuario
 */
data class ClimaResponse(
    val temperatura: Double,
    val viento: Double,
    val humedad: Int,
    val codigo: Int,
    val unidad_temp: String,
    val unidad_viento: String,
    val sensacion_termica: Double,
    val indice_uv: Double,
    val visibilidad_km: Double,
    val direccion_viento: String,
    val presion_mb: Double,
    val es_de_dia: Boolean,
    val precipitacion_mm: Double,
    val texto_clima: String,
    val municipio: String,
    val region: String,
)

/**
 * Cuerpo de la petición para enviar el código de verificación por correo
 */
data class VerificacionRequest(
    val email: String,
    val uid: String
)

/** Cuerpo de la petición para iniciar la recuperación de contraseña */
data class RecuperarContraseniaRequest(
    val email: String
)

/** Cuerpo de la petición para enviar el correo de bienvenida al registrarse */
data class BienvenidaRequest(
    val email: String
)

/**
 * Respuesta del webhook de verificación.
 */
data class VerificacionResponse(
    val success: Boolean,
    val codigo: String
)

/**
 * Interfaz que define los endpoints del servidor n8n de la aplicación
 */
interface N8nApiService {
    /** Obtiene la lista de correos asociado al token de la cuenta */
    @GET("webhook/obtener-emails")
    suspend fun obtenerCorreos(
        @Query("token") token: String
    ): List<Correo>

    /** Envía un correo usando el flujo de redacción */
    @POST("webhook/enviar-email")
    suspend fun enviarCorreo(
        @Body body: EnviarCorreoRequest
    ): retrofit2.Response<Unit>

    /** Obtiene los datos meteorológicos actuales para las coordenadas lat y lon*/
    @GET("webhook/obtener-clima")
    suspend fun obtenerClima(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): ClimaResponse

    /** Recupera el cuerpo completo de un correo por su id */
    @GET("webhook/d0521b1f-a7b6-4bf0-9292-940850a29e93/correo-detalle/correo/{id}")
    suspend fun obtenerCorreoPorId(
        @Path("id") id: String,
        @Query("token") token: String
    ): Correo

    /** Envia el código de verificacion al iniciar sesion*/
    @POST("webhook/enviar-verificacion")
    suspend fun enviarCodigoVerificacion(
        @Body body: VerificacionRequest
    ): VerificacionResponse

    /** Envía el correo de bienvenida tras registrarse el usuario */
    @POST("webhook/bienvenida")
    suspend fun enviarCorreoBienvenida(
        @Body body: BienvenidaRequest
    ): retrofit2.Response<Unit>
}

/**
 * Configura el cliente de red para comunicarse con n8n,
 * gestionando tiempos de espera y la conversión de datos JSON.
 */
object N8nClient {
    const val BASE_URL = "http://192.168.1.49:5678/"

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