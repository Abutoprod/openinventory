package com.openinventory.app.ui.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ScannerViewModel : ViewModel() {

    var scanResult = mutableStateOf("")
        private set

    fun onScan(value: String) {
        scanResult.value = value
    }
}