package com.openinventory.app.data.repository
import com.openinventory.app.service.RayearthApiService
import com.openinventory.app.core.config.TokenManager
import com.openinventory.app.service.ComandaResponseDTO

class DashboardRepository(private val apiService: RayearthApiService) {

    private fun getAuthToken(): String = TokenManager.getBearerToken() ?: ""

    suspend fun buscarDadosVendas(filialId: Long, dataInicio: String, dataFim: String): List<ComandaResponseDTO> {
        return try {
            val response = apiService.listarTodasComandas(
                token = getAuthToken(),
                filialId = filialId,
                status = "fechadas",
                clienteId = null// Filtramos apenas o que já foi pago/fechado
                // Aqui passarias as datas se a tua API já aceitar filtros de data
            )
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}