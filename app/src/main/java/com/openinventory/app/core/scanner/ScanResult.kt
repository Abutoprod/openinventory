package com.openinventory.app.core.scanner

data class ScanResult (
    val barcode: String,
    val timestamp: Long = System.currentTimeMillis()
)