package com.openinventory.app.data.repository

import android.util.Log
import com.openinventory.app.core.config.TokenManager
import com.openinventory.app.service.ComandaResponseDTO
import com.openinventory.app.service.RayearthApiService
import com.openinventory.app.service.ProductResponse
import com.openinventory.app.service.UsuarioResponse
import retrofit2.Response
import com.openinventory.app.service.ItemVendaDTO

import com.openinventory.app.service.VendaRapidaRequest
class ComandaRepository(private val apiService: RayearthApiService) {

    private fun getAuthToken(): String {
        val t = TokenManager.getBearerToken() ?: ""
        return if (t.startsWith("Bearer ")) t else "Bearer $t"
    }

    suspend fun getComandasFiltradas(
        filialId: Long,
        status: String?,
        clienteId: Long?
    ): List<ComandaResponseDTO> {
        return try {
            val response = apiService.listarTodasComandas(
                token = getAuthToken(),
                filialId = filialId,
                status = status,
                clienteId = clienteId
            )
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            Log.e("Repo", "Erro ao filtrar: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUsuarios(): List<UsuarioResponse> {
        return try {
            val response = apiService.listarUsuarios(getAuthToken())
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("Repo", "Erro API Usuarios: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Repo", "Falha de rede Usuarios: ${e.message}")
            emptyList()
        }
    }

    suspend fun adicionarItem(comandaId: Long, codigoProduto: String, quantidade: Int): Boolean {
        return try {
            val response = apiService.adicionarItem(
                token = getAuthToken(),
                comandaId = comandaId,
                codigoProduto = codigoProduto,
                quantidade = quantidade
            )

            // Se for 200 OK, retorna true mesmo que o corpo não seja um JSON válido
            response.isSuccessful
        } catch (e: Exception) {
            // Agora ele não deve mais cair aqui por erro de JSON malformado
            Log.e("ComandaRepo", "Erro: ${e.message}")
            false
        }
    }
    suspend fun getDetalhesComanda(id: Long): ComandaResponseDTO? {
        return try {
            val response = apiService.buscarComandaPorId(getAuthToken(), id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    suspend fun fecharComanda(comandaId: Long): Response<String> {
        // Usamos o seu getAuthToken() que já utiliza o TokenManager correto do seu projeto
        val token = getAuthToken()
        return apiService.fecharComanda(token, comandaId)
    }

    suspend fun getProdutosPorFilial(filialId: Long): List<ProductResponse> {
        return try {
            val response = apiService.getProdutosPorFilial(filialId.toString())
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    // No ComandaRepository.kt, adiciona:

    suspend fun finalizarVendaRapida(
        filialId: Long,
        clienteId: Long,
        itens: List<com.openinventory.app.ui.sale.ItemCarrinho>
    ): Boolean {
        return try {
            // Monta o objeto exatamente como o seu Java espera
            val request = VendaRapidaRequest(
                filialId = filialId,
                clienteId = clienteId,
                itens = itens.map {
                    ItemVendaDTO(
                        // SE O SEU JAVA PEDE "codigoProduto", troque 'id' por 'codigoProduto' aqui
                        id = it.produto.id,
                        quantidade = it.quantidade
                    )
                }
            )

            val token = getAuthToken() // Ex: "Bearer eyJhbG..."

            // Log do JSON que vai sair (graças ao Interceptor que colocamos antes)
            val response = apiService.realizarVendaRapida(token, request)

            if (response.isSuccessful) {
                // .string() lê o texto "Venda rápida concluída! ID: 43..."
                val textoSucesso = response.body()?.string()
                android.util.Log.d("RepoVenda", "Sucesso do Java: $textoSucesso")
                true // RETORNA TRUE PORQUE O STATUS FOI 200
            } else {
                val erroBody = response.errorBody()?.string()
                android.util.Log.e("RepoVenda", "Erro ${response.code()}: $erroBody")
                false
            }
        } catch (e: Exception) {
            // Se cair aqui, é porque houve erro de rede ou o GSON falhou
            android.util.Log.e("RepoVenda", "Falha: ${e.message}")
            false
        }
    }
    suspend fun abrirComanda(usuarioId: Long, filialId: Long): Boolean {
        return try {
            val response = apiService.abrirComanda(
                token = getAuthToken(),
                usuarioId = usuarioId,
                filialId = filialId
            )
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Repo", "Erro ao abrir: ${e.message}")
            false
        }
    }


}