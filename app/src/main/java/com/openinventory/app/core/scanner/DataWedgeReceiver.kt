package com.openinventory.app.core.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DataWedgeReceiver(
    private val onScan: (String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getStringExtra("com.symbol.datawedge.data_string")

        if (!data.isNullOrEmpty()) {
            onScan(data)
        }
    }
}