package com.example.plantme_grupo8.api
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ==========================================
// 1. INTERFAZ DE LA API (Los Endpoints)
// ==========================================
interface ApiService {

    // --- AUTENTICACIÓN ---
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<JwtResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    // --- PLANTAS (Requieren Token) ---
    @POST("api/plantas")
    suspend fun createPlant(
        @Header("Authorization") token: String,
        @Body request: PlantRequest
    ): Response<PlantResponse>

    @GET("api/plantas")
    suspend fun getPlants(
        @Header("Authorization") token: String
    ): Response<List<PlantResponse>>
}

// ==========================================
// 2. CLIENTE RETROFIT (El objeto Singleton)
// ==========================================
object RetrofitClient {
    // ⚠️ IMPORTANTE:
    // Usa "http://10.0.2.2:8080/" si usas el Emulador de Android Studio.
    // Usa la IP de tu PC (ej: "http://192.168.1.15:8080/") si usas celular físico.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ==========================================
// 3. MODELOS DE DATOS (DTOs)
// ¡AQUÍ ESTÁ LO QUE TE FALTABA! 👇
// ==========================================

// Login
data class LoginRequest(
    val email: String,
    val password: String
)

data class JwtResponse(
    val token: String
)

// Registro
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

// Plantas (Lo que te daba error)
data class PlantRequest(
    val nombre: String,
    val speciesKey: String,
    val ultimoRiego: String
)

data class PlantResponse(
    val id: Long,
    val nombre: String,
    val speciesKey: String,
    val frecuenciaDias: Int,
    val siguienteRiego: String,
    val ultimoRiego: String
)