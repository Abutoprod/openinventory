package com.openinventory.app.ui.import

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import  kotlinx.coroutines.flow.*
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
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            try {
                val count = repository.importCsv(uri) // Pega o número de linhas

                if (count > 0) {
                    _uiState.value = ImportUiState.Success(count)
                } else {
                    // Se chegou aqui, o arquivo foi lido mas não tinha produtos válidos
                    _uiState.value = ImportUiState.Error("Nenhum produto válido encontrado no arquivo.")
                }
            } catch (e: Exception) {
                android.util.Log.e("IMPORT_DEBUG", "ERRO REAL: ${e.message}", e)
                _uiState.value = ImportUiState.Error("Falha ao importar: ${e.message}")
            }
        }
    }

    fun resetState() {
        // CORREÇÃO: Use _uiState.value
        _uiState.value = ImportUiState.Idle
    }
}