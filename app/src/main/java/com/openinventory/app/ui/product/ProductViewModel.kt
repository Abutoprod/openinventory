package com.openinventory.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.service.ProductResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.openinventory.app.service.ProductDTO

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _currentStoreId = MutableStateFlow("")
    val currentStoreId: StateFlow<String> = _currentStoreId.asStateFlow()

    private val _currentStoreName = MutableStateFlow("Carregando...")
    val currentStoreName: StateFlow<String> = _currentStoreName.asStateFlow()

    // Sempre que o ID da loja mudar, este Flow busca os produtos novos na API
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductResponse>> = _currentStoreId
        .filter { it.isNotEmpty() }
        .flatMapLatest { storeId ->
            flow {
                Log.d("DEBUG_VM", "Iniciando fluxo de busca para filial: $storeId")
                emit(repository.getProductsByStore(storeId))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateStore(newStoreId: String) {
        if (_currentStoreId.value != newStoreId) {
            Log.d("DEBUG_VM", "Atualizando ID da loja no ViewModel para: $newStoreId")
            _currentStoreId.value = newStoreId
            fetchStoreName(newStoreId)
        }
    }

    fun salvarProduto(dto: ProductDTO) {
        viewModelScope.launch {
            try {
                // 1. Faz a chamada de POST
                val sucesso = repository.salvarProduto(dto)

                if (sucesso) {
                    // 2. Se salvou no banco com sucesso, chama a busca de novo
                    // Isso atualiza o StateFlow e faz a tela dar o "refresh"
                    refreshData()
                    Log.d("ProductVM", "Produto cadastrado e lista atualizada")
                } else {
                    Log.e("ProductVM", "Erro ao salvar produto no servidor")
                }
            } catch (e: Exception) {
                Log.e("ProductVM", "Falha na comunicação: ${e.message}")
            }
        }
    }
    fun alterarProduto(id: Long, dto: ProductDTO) {
        viewModelScope.launch {
            try {
                // 1. Faz a chamada de PUT/PATCH para atualizar
                val sucesso = repository.atualizarProduto(id, dto)

                if (sucesso) {
                    // 2. Se atualizou no banco com sucesso, chama o refreshData
                    // Isso garante que a tela mostre os novos dados imediatamente

                    Log.d("ProductVM", "Produto ID: $id alterado e lista atualizada")
                } else {
                    Log.e("ProductVM", "Erro ao alterar produto no servidor (Resposta não positiva)")

                }
                refreshData()
            } catch (e: Exception) {
                // Captura erros de internet, timeout ou conversão de dados
                Log.e("ProductVM", "Falha na comunicação ao alterar: ${e.message}")
            }
        }
    }

    // Dentro do ProductViewModel.kt, corrija a função fetchStoreName:
    private fun fetchStoreName(id: String) {
        viewModelScope.launch {
            try {
                val filiais = repository.getFiliais() // Agora o repositório tem essa função
                val filial = filiais.find { it.id.toString() == id } // it agora funciona
                _currentStoreName.value = filial?.nome ?: "Filial $id"
            } catch (e: Exception) {
                _currentStoreName.value = "Filial $id"
            }
        }
    }

    fun refreshData() {
        val current = _currentStoreId.value
        if (current.isNotEmpty()) {
            Log.d("DEBUG_VM", "Refresh manual solicitado")
            _currentStoreId.value = "" // Reseta para forçar o trigger do Flow
            _currentStoreId.value = current
        }
    }
}