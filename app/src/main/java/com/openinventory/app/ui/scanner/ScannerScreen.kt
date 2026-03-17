package com.openinventory.app.ui.scanner

import android.content.IntentFilter
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.openinventory.app.core.scanner.DataWedgeReceiver

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {

    val context = LocalContext.current

    DisposableEffect(Unit) {

        val receiver = DataWedgeReceiver {
            viewModel.onScan(it)
        }

        val filter = IntentFilter("com.stankovic.zebrascanner.scan")

        context.registerReceiver(receiver, filter,Context.RECEIVER_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Text(text = "Scan: ${viewModel.scanResult.value}")
}