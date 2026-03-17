package com.openinventory.app.core.scanner
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class ScannerManager (private val context: Context){
    private var listener: ((ScanResult) -> Unit)? = null
    private val reciever = ScannerBroadcastReceiver{
        listener?.invoke(it)
    }
    fun startScanner(){
        val filter = IntentFilter()
        filter.addAction("com.openinventory.app.SCAN")
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION")
        filter.addAction("com.scanner.broadcast")
        
        ContextCompat.registerReceiver(
            context,
            reciever,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

    }
    fun stopScanner(){
        context.unregisterReceiver(reciever)
    }
    fun setListener(listener: (ScanResult) -> Unit) {
        this.listener = listener
    }
}