package com.openinventory.app.ui.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import com.openinventory.app.core.scanner.ScannedProduct

class ScannerViewModel(private val scannerManager: ScannerManager) : ViewModel() {

    var scannedProducts = mutableStateListOf<ScannedProduct>()
        private set

    val totalItems: Int get() = scannedProducts.size
    var scanResult = mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            scannerManager.scanFlow.collect { barcode ->
                scannedProducts.add(0, ScannedProduct(barcode,"Agora"))
                /*scanResult.value = barcode
                onScan(barcode) // Sua lógica de negócio aqui*/
            }
        }
    }
    fun deleteProduct(product: ScannedProduct) {
        scannedProducts.remove(product)
    }

    fun onScan(value: String) {
        scanResult.value = value
    }
}