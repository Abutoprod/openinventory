package com.openinventory.app.core.scanner

interface ScannerDevice {
    fun startScanner()
    fun stopScanner()
    fun setListener(listener: (ScanResult) -> Unit)
}