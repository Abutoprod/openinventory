package com.openinventory.app.core.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScannerBroadcastReceiver(
    private val onScan: (ScanResult) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // 🔥 DEBUG COMPLETO DO INTENT
        intent?.extras?.keySet()?.forEach {
            Log.d("DW_DEBUG", "Key: $it -> ${intent.extras?.get(it)}")
        }

        // 🔎 tenta pegar o barcode de várias formas
        val barcode =
            intent?.getStringExtra("data")
                ?: intent?.getStringExtra("com.symbol.datawedge.data_string")
                ?: intent?.getStringExtra("barcode")

        Log.d("SCANNER_TEST", "Recebido: $barcode")

        barcode?.let {
            onScan(ScanResult(it))
        }
    }
}