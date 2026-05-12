package com.openinventory.app.ui.dashboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.openinventory.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // <--- ESTE IMPORT É O QUE FAZ O .update FUNCIONAR
import kotlinx.coroutines.launch

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()


    fun carregarDados(filialId: Long, dataInicioFiltro: String? = null, dataFimFiltro: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Busca todas as comandas (como você já faz)
                val todasComandas = repository.buscarDadosVendas(filialId, "", "")

                // 2. Filtra localmente por data se o usuário escolheu um período
                val comandasFiltradas = if (!dataInicioFiltro.isNullOrBlank() && !dataFimFiltro.isNullOrBlank()) {
                    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    val inicio = LocalDateTime.parse("${dataInicioFiltro}T00:00:00")
                    val fim = LocalDateTime.parse("${dataFimFiltro}T23:59:59")

                    todasComandas.filter { comanda ->
                        val dataComanda = LocalDateTime.parse(comanda.dataAbertura, formatter)
                        !dataComanda.isBefore(inicio) && !dataComanda.isAfter(fim)
                    }
                } else {
                    todasComandas
                }

                // 3. Processa os cálculos em cima da lista filtrada
                var faturamento = 0.0
                var custoVendas = 0.0
                val produtosMap = mutableMapOf<String, Float>()

                comandasFiltradas.forEach { comanda ->
                    if (!comanda.aberta) {
                        faturamento += comanda.valorTotal
                        comanda.itens.forEach { item ->
                            custoVendas += (item.precoCompra.toDouble() * item.quantidade)
                            val atual = produtosMap.getOrDefault(item.produtoNome, 0f)
                            produtosMap[item.produtoNome] = atual + item.quantidade
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        totalRecebido = faturamento,
                        totalCusto = custoVendas,
                        lucro = faturamento - custoVendas,
                        itensPizza = produtosMap.map { entry -> PieEntry(entry.value, entry.key) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}