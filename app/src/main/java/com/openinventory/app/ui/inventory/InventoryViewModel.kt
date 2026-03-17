package com.openinventory.app.ui.inventory
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import com.openinventory.app.core.scanner.DataWedgeHelper
import kotlinx.coroutines.flow.StateFlow

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scannerManager = ScannerManager(application)

    private val _lastScan = MutableStateFlow("")
    val lastScan: StateFlow<String> = _lastScan

    init {
        DataWedgeHelper.createProfile(getApplication())
        scannerManager.setListener {
            _lastScan.value = it.barcode
        }

        scannerManager.startScanner()
    }

    override fun onCleared() {
        super.onCleared()
        scannerManager.stopScanner()
    }
}