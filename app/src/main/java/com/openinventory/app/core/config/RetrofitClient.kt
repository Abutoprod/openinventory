package com.openinventory.app.core.config
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.openinventory.app.service.RayearthApiService


object RetrofitClient {
    private const val BASE_URL = "https://perennial-camisole-unaired.ngrok-free.dev/api/"
    val instance: RayearthApiService by lazy {
        // 1. Criamos o interceptor de LOG
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Criamos o cliente OkHttp e adicionamos o interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // 3. Configuramos o Retrofit usando esse cliente
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // <--- ESSA LINHA FAZ O LOG FUNCIONAR
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RayearthApiService::class.java)
    }
}