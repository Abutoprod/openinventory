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
        Log.d("SCANNER", "START chamado")
        val filter =
            IntentFilter("com.stankovic.zebrascanner.scan") // aqui é configurado no datawdge
        context.registerReceiver(
            dataWedgeReceiver,
            filter,
            Context.RECEIVER_EXPORTED
        )
    }

    fun stop() {
        context.unregisterReceiver(dataWedgeReceiver)
    }
}
