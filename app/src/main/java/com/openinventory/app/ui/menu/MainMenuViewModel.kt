package com.openinventory.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.config.RetrofitClient
import com.openinventory.app.service.FilialResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
class MainMenuViewModel : ViewModel() {
    private val _filiais = MutableStateFlow<List<FilialResponse>>(emptyList())
    val filiais = _filiais.asStateFlow()

    init {
        carregarFiliais()
    }

    fun carregarFiliais() {
        val token = com.openinventory.app.core.config.TokenManager.getBearerToken()

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getFiliais()
                if (response.isSuccessful) {
                    // FILTRAGEM: Apenas as filiais onde ativo == true
                    val listaAtivas = response.body()?.filter { it.ativo } ?: emptyList()
                    _filiais.value = listaAtivas
                }
            } catch (e: Exception) {
                // Log de erro para debug
                Log.e("MainMenuVM", "Erro ao carregar filiais", e)
            }
        }
    }
}