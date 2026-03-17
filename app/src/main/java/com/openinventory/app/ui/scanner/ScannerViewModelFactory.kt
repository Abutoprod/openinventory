package com.openinventory.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openinventory.app.core.scanner.ScannerManager

class ScannerViewModelFactory(private val scannerManager: ScannerManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(scannerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}