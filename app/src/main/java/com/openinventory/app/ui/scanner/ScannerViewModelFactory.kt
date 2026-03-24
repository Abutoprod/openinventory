package com.openinventory.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.data.repository.ProductRepository
class ScannerViewModelFactory(private val scannerManager: ScannerManager,private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(scannerManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}