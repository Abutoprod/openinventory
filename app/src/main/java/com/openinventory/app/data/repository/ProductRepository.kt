package com.openinventory.app.data.repository

import android.util.Log
import com.openinventory.app.core.config.TokenManager
import com.openinventory.app.service.FilialResponse
import com.openinventory.app.service.ProductResponse
import com.openinventory.app.service.RayearthApiService
import com.openinventory.app.service.ProductDTO

class ProductRepository(private val apiService: RayearthApiService) {

    // Dentro do seu ProductRepository.kt
    suspend fun getProductsByStore(storeId: String): List<ProductResponse> {
        return try {
            // 1. Pega o token já com "Bearer "
            val tokenFormatado = TokenManager.getBearerToken()

            // 2. Passa direto para a API sem mexer na String
            val response = apiService.getProdutosPorFilial(
              //  token = tokenFormatado,
                filialId = storeId
            )

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                android.util.Log.e("DEBUG_API", "Erro: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // No ProductRepository.kt
    suspend fun salvarProduto(novoProduto: ProductDTO): Boolean {
        return try {
            val token = TokenManager.getBearerToken()
            // Agora o nome 'produto' casa com a interface acima
            val response = apiService.postProdutosPorFilial(
                token = token,
                produto = novoProduto
            )

            if (!response.isSuccessful) {
                Log.e("DEBUG_API", "Erro ao salvar: ${response.code()} - ${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("DEBUG_API", "Falha catastrófica: ${e.message}")
            false
        }
    }

    suspend fun atualizarProduto(id: Long, dto: ProductDTO): Boolean {
        return try {
            val token = TokenManager.getBearerToken()
            val response = apiService.atualizarProduto(token, id, dto)
            response.isSuccessful
        } catch (e: Exception) { false }
    }
    suspend fun getFiliais(): List<FilialResponse> {
        val token = com.openinventory.app.core.config.TokenManager.getBearerToken()
        val response = apiService.getFiliais()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}