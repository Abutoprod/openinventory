package com.openinventory.app.ui.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.launch

class ScannerViewModel(private val scannerManager: ScannerManager) : ViewModel() {

    var scanResult = mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            scannerManager.scanFlow.collect { barcode ->
                scanResult.value = barcode
                onScan(barcode) // Sua lógica de negócio aqui
            }
        }
    }
    fun onScan(value: String) {
        scanResult.value = value
    }
}