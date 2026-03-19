package com.openinventory.app.ui.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import com.openinventory.app.core.scanner.ScannedProduct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import androidx.compose.ui.platform.LocalContext

class ScannerViewModel(private val scannerManager: ScannerManager) : ViewModel() {
    private var lastScannedCode: String? = null
    private var lastScanTime: Long = 0

    var scannedProducts = mutableStateListOf<ScannedProduct>()
        private set

    val totalItems: Int get() = scannedProducts.size
    var scanResult = mutableStateOf("")
        private set


    init {
        viewModelScope.launch {
            scannerManager.scanFlow.collect { barcode ->
                onProductScanned(barcode)
            }
        }
    }

    fun onProductScanned(barcode: String) {
        val currentTime = System.currentTimeMillis()

        // Adiciona no topo da lista (índice 0)
        if (barcode == lastScannedCode && (currentTime - lastScanTime) < 2500) {
            return
        }
        // Atualiza os marcadores para a próxima leitura
        lastScannedCode = barcode
        lastScanTime = currentTime

        // 3. Adiciona na lista com o horário atual
        val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        scannedProducts.add(0, ScannedProduct(barcode, timestamp))
        scanResult.value = barcode
    }
    fun deleteProduct(product: ScannedProduct) {
        scannedProducts.remove(product)
    }

    fun onScan(value: String) {
        scanResult.value = value
    }
}