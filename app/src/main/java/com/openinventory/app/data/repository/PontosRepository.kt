package com.openinventory.app.data.repository
import android.util.Log
import com.openinventory.app.core.config.TokenManager
import com.openinventory.app.service.RayearthApiService
import com.openinventory.app.service.RankingDTO
import com.openinventory.app.service.LancamentoDTO
import com.openinventory.app.service.UsuarioResponse
import kotlin.collections.emptyList

class PontosRepository(private val apiService: RayearthApiService) {

    private fun getAuthToken(): String = TokenManager.getBearerToken() ?: ""

    suspend fun buscarRanking(
        jogoId: Long,
        filialId: Long,
        mes: Int? = null,
        ano: Int? = null
    ): List<RankingDTO> {
        return try {
            apiService.consultarRanking(
                token = getAuthToken(),
                jogoId = jogoId,
                filialId = filialId,
                mes = mes,
                ano = ano
            )
        } catch (e: Exception) {
            Log.e("Repo", "Erro ao buscar ranking: ${e.message}")
            emptyList()
        }
    }

    suspend fun buscarClientes(): List<UsuarioResponse> {
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

    suspend fun lancarPontos(dados: LancamentoDTO): Boolean {
        return try {
            val response = apiService.lancarPontos(getAuthToken(), dados)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}