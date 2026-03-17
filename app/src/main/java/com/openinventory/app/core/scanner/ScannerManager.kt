package com.openinventory.app.core.scanner

import android.content.IntentFilter
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import android.util.Log
class ScannerManager(private val context: Context) {
    private val _scanFlow = MutableSharedFlow<String>()
    val scanFlow = _scanFlow.asSharedFlow()

    private val dataWedgeReceiver = DataWedgeReceiver {
        _scanFlow.tryEmit(it)
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction("com.openinventory.app.scan")
            addCategory("android.intent.category.DEFAULT") // Categoria explícita
        }

                context.registerReceiver(dataWedgeReceiver, filter, Context.RECEIVER_EXPORTED)
        Log.d("SCANNER", "Receiver registrado para a action: com.openinventory.app.scan")
    }

    fun stop() {
        context.unregisterReceiver(dataWedgeReceiver)
    }
}
