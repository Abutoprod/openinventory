package com.openinventory.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.config.CompanyConstants
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // 1. O segredo está aqui: um Flow que guarda o ID da loja atual
    private val _currentStoreFilter = MutableStateFlow(CompanyConstants.currentStoreId)

    // 2. Sempre que o _currentStoreFilter mudar, ele busca os produtos novos
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = _currentStoreFilter
        .flatMapLatest { storeId ->
            Log.d("VM_DEBUG", "Mudando para a filial: $storeId")
            repository.getProductsByStore(storeId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshStoreFilter()
    }

    // Função que você deve chamar no LaunchedEffect da tela de Estoque
    fun refreshStoreFilter() {
        _currentStoreFilter.value = CompanyConstants.currentStoreId
        refreshFromFirebase()
    }

    fun refreshFromFirebase() {
        viewModelScope.launch {
            try {
                val currentStore = CompanyConstants.currentStoreId
                repository.syncWithFirebase(currentStore)
                Log.d("VM_DEBUG", "Sincronizando com Firebase: $currentStore")
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro ao atualizar: ${e.message}")
            }
        }
    }

    fun saveNewProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStore = CompanyConstants.currentStoreId
            val correctedProduct = product.copy(storeId = currentStore)
            repository.insertProduct(correctedProduct, currentStore)
        }
    }

    fun updateStock(sku: String, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentStore = CompanyConstants.currentStoreId
                repository.updateProductQuantity(sku, newQuantity, currentStore)
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro no estoque: ${e.message}")
            }
        }
    }

    fun importCsvData(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStore = CompanyConstants.currentStoreId
            repository.importCsv(uri, currentStore)
            refreshFromFirebase()
        }
    }
}