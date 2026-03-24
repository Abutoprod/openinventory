package com.openinventory.app.ui.import
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.openinventory.app.data.repository.ProductRepository
class ImportViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}