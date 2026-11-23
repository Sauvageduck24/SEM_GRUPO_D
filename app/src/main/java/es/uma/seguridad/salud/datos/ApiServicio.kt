package es.uma.seguridad.salud.datos

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import es.uma.seguridad.salud.BuildConfig
import es.uma.seguridad.salud.modelo.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

/* ==========================================================================
   EL ACCESO AL BACKEND
   Teoría: sesión 2.5.5 (red) y 2.4.5 (corrutinas).

   La URL y la política de confianza se configuran al compilar la variante.
   Android aplica la política de red a la conexión HTTPS.
   ========================================================================== */

/**
 * La interfaz describe la API: una función por endpoint. Retrofit genera en
 * tiempo de ejecución la clase que hace las peticiones de verdad, así que aquí
 * no hay ni una línea de código de red.
 *
 * Todas son `suspend`, de modo que sólo pueden llamarse desde una corrutina y
 * nunca bloquean el hilo de la interfaz.
 */
interface ApiServicio {

    /** El cuerpo se serializa a JSON a partir de la clase de datos. */
    @POST("api/login")
    suspend fun login(@Body peticion: PeticionLogin): RespuestaLogin

    /**
     * La cabecera de autorización viaja como parámetro: quien llame tiene que
     * componer el valor "Bearer <token>". Lo hace el Repositorio.
     */
    @GET("api/elementos")
    suspend fun elementos(@Header("Authorization") autorizacion: String): RespuestaRegistrosSalud

    @GET("api/elementos/{id}")
    suspend fun elemento(@Header("Authorization") autorizacion: String, @Path("id") id: Int): RegistroSalud

    @GET("api/perfil")
    suspend fun perfil(@Header("Authorization") autorizacion: String): Perfil
}

object ClienteRed {

    const val BASE_URL = BuildConfig.BACKEND_URL

    fun crear(): ApiServicio {

        // P3.1 (paso 2c): BASIC solo muestra método, URL, código y tamaño.
        // Ni cabeceras (Authorization) ni cuerpos (contraseña, token, datos).
        val registro = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val cliente = OkHttpClient.Builder()
            // Evita trasladar una petición a un origen con otra política de confianza.
            .followRedirects(false)
            .addInterceptor(registro)
            .build()

        /*
         * ignoreUnknownKeys = true evita que la aplicación se caiga si el
         * servidor añade un campo que nosotros no conocemos. Es tolerancia
         * razonable, y no debe confundirse con validar la respuesta, que es
         * otra cosa y se trata en la sesión 2.5.5.
         */
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiServicio::class.java)
    }
}
