package com.openinventory.app.core.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScannerBroadcastReceiver(
    private val onScan: (ScanResult) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("DW_DEBUG", "ACTION: ${intent?.action}")

        intent?.extras?.keySet()?.forEach {
            Log.d("DW_DEBUG", "Key: $it -> ${intent.extras?.get(it)}")
        }

        val data = intent?.getStringExtra(ScannerConstants.DATA_KEY)

        if (!data.isNullOrEmpty()) {
            onScan(ScanResult(data))
        }
    }
}