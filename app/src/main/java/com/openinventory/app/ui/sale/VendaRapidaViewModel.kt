package com.openinventory.app.ui.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.ComandaRepository
import com.openinventory.app.service.ProductResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ItemCarrinho(
    val produto: ProductResponse,
    val quantidade: Int
)

class VendaRapidaViewModel(private val repository: ComandaRepository) : ViewModel() {
    private val _carrinho = MutableStateFlow<List<ItemCarrinho>>(emptyList())
    val carrinho = _carrinho.asStateFlow()

    private val _produtosLoja = MutableStateFlow<List<ProductResponse>>(emptyList())
    val produtosLoja = _produtosLoja.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    fun carregarProdutos(filialId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val produtos = repository.getProdutosPorFilial(filialId)
            _produtosLoja.value = produtos
            _isLoading.value = false
        }
    }
    fun adicionarAoCarrinho(produto: ProductResponse) {
        val listaAtual = _carrinho.value.toMutableList()
        val index = listaAtual.indexOfFirst { it.produto.id == produto.id }

        if (index != -1) {
            val itemAntigo = listaAtual[index]
            listaAtual[index] = itemAntigo.copy(quantidade = itemAntigo.quantidade + 1)
        } else {
            listaAtual.add(ItemCarrinho(produto, 1))
        }
        _carrinho.value = listaAtual
    }

    fun removerOuDiminuir(produto: ProductResponse) {
        val listaAtual = _carrinho.value.toMutableList()
        val index = listaAtual.indexOfFirst { it.produto.id == produto.id }

        if (index != -1) {
            val item = listaAtual[index]
            if (item.quantidade > 1) {
                listaAtual[index] = item.copy(quantidade = item.quantidade - 1)
            } else {
                listaAtual.removeAt(index)
            }
            _carrinho.value = listaAtual
        }
    }

    fun confirmarVenda(filialId: Long, onSuccess: () -> Unit) {
        android.util.Log.d("VendaRapidaVM", "Iniciando processo de confirmação...")

        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Usando o cliente 99999 que combinamos
                val sucesso = repository.finalizarVendaRapida(filialId, 99999L, _carrinho.value)

                android.util.Log.d("VendaRapidaVM", "Resultado do repositório: $sucesso")

                if (sucesso) {
                    _carrinho.value = emptyList()
                    onSuccess()
                } else {
                    android.util.Log.e("VendaRapidaVM", "Erro: O repositório retornou falso")
                }
            } catch (e: Exception) {
                android.util.Log.e("VendaRapidaVM", "EXCEÇÃO: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}