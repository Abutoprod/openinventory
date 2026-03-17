package com.openinventory.app.ui.inventory
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.scanner.ScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import com.openinventory.app.core.scanner.DataWedgeHelper
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {

    private val _lastScan = MutableStateFlow("")
    val lastScan: StateFlow<String> = _lastScan

    fun onScan(code: String) {
        _lastScan.value = code
    }
}