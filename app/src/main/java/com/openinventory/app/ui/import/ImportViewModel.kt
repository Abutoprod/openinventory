package com.openinventory.app.ui.import

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.config.CompanyConstants
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    data class Success(val count: Int) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

class ImportViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun importFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) { // Importante rodar em IO para não travar a UI
            _uiState.value = ImportUiState.Loading
            try {
                // 1. Pega a filial atual do arquivo de configuração global
                val currentStore = CompanyConstants.currentStoreId

                // 2. Chama a importação e guarda o retorno (quantidade de itens)
                val totalImported = repository.importCsv(uri, currentStore)

                // 3. Atualiza o estado com base no resultado
                if (totalImported > 0) {
                    _uiState.value = ImportUiState.Success(totalImported)
                } else {
                    _uiState.value = ImportUiState.Error("Nenhum produto válido encontrado no arquivo.")
                }
            } catch (e: Exception) {
                Log.e("IMPORT_DEBUG", "ERRO AO IMPORTAR: ${e.message}", e)
                _uiState.value = ImportUiState.Error("Falha ao importar: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}