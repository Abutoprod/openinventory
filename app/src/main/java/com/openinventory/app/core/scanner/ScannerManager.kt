
package com.openinventory.app.core.scanner

import android.content.Context
import android.content.IntentFilter

class ScannerManager(private val context: Context) {

    private var listener: ((ScanResult) -> Unit)? = null

    private val receiver = ScannerBroadcastReceiver {
        listener?.invoke(it)
    }

    fun startScanner() {
        val filter = IntentFilter().apply {
            addAction(ScannerConstants.SCAN_ACTION)
        }

        context.registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_EXPORTED
        )
    }

    fun stopScanner() {
        context.unregisterReceiver(receiver)
    }

    fun setListener(listener: (ScanResult) -> Unit) {
        this.listener = listener
    }
}