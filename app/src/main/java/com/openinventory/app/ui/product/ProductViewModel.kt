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
import com.openinventory.app.core.config.CompanyConstants
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
        // 2. Sempre que a ViewModel nasce (abriu a tela), tenta sincronizar a filial atual
        refreshFromFirebase()
    }

    fun refreshFromFirebase() {
        viewModelScope.launch {
            try {
                val currentStore = CompanyConstants.currentStoreId
                repository.syncWithFirebase(currentStore)
                Log.d("VM_DEBUG", "Refresh solicitado para a filial: $currentStore")
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro ao atualizar dados: ${e.message}")
            }
        }
    }

    fun saveNewProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Passa a filial atual para o repositório salvar corretamente no Firebase
                val currentStore = CompanyConstants.currentStoreId
                repository.insertProduct(product, currentStore)
                Log.d("VM_DEBUG", "Produto ${product.description} salvo com sucesso na filial $currentStore.")
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro ao salvar produto: ${e.message}")
            }
        }
    }

    // 3. Função para dar baixa ou atualizar estoque via UI
    fun updateStock(sku: String, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentStore = CompanyConstants.currentStoreId
                repository.updateProductQuantity(sku, newQuantity, currentStore)
                Log.d("VM_DEBUG", "Estoque atualizado: SKU $sku para $newQuantity na filial $currentStore")
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Erro ao atualizar estoque: ${e.message}")
            }
        }
    }

    // 4. Importação de CSV vinculada à loja
    fun importCsvData(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStore = CompanyConstants.currentStoreId
            val importedCount = repository.importCsv(uri, currentStore)
            Log.d("VM_DEBUG", "Importação finalizada: $importedCount itens para a filial $currentStore")

            // Após importar, força um refresh para garantir que o UI está batendo com o novo estado
            refreshFromFirebase()
        }
    }
}