package com.openinventory.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Dispatchers

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // 1. Expõe os produtos do Room como um StateFlow reativo
    // O 'stateIn' converte o Flow do Room em um estado que o Compose entende
    val products: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // 2. Sempre que a ViewModel nasce (abriu a tela), tenta sincronizar
        refreshFromFirebase()
    }

    fun refreshFromFirebase() {
        viewModelScope.launch {
            repository.syncWithFirebase()
        }
    }

    fun saveNewProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertProduct(product)
                // Opcional: Log de sucesso
                Log.d("VM_DEBUG", "Produto ${product.description} salvo com sucesso.")
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro ao salvar produto: ${e.message}")
            }
        }
    }

    // 3. Função para dar baixa ou atualizar estoque via UI
    fun updateStock(sku: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateProductQuantity(sku, newQuantity)
        }
    }
}