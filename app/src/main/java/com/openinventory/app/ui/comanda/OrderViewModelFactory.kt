package com.openinventory.app.ui.comanda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openinventory.app.data.repository.ComandaRepository

// No seu OrderViewModelFactory (ou onde você cria o ViewModel)
class OrderViewModelFactory(private val repository: ComandaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComandaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComandaViewModel(repository) as T // DEVE PASSAR O REPOSITORY
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}