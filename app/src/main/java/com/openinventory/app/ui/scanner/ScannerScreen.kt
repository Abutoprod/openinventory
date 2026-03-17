package com.openinventory.app.ui.scanner

import android.content.IntentFilter
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import com.openinventory.app.core.scanner.DataWedgeReceiver
import com.openinventory.app.core.scanner.ScannerManager
@Composable
fun ScannerScreen(viewModel: ScannerViewModel, scannerManager: ScannerManager) {

    // Liga o scanner quando entra na tela e desliga ao sair
    DisposableEffect(Unit) {
        scannerManager.start()
        onDispose {
            scannerManager.stop()
        }
    }
    Column {
        Text(text = "Aponte para o código de barras")
        Text(text = "Scan: ${viewModel.scanResult.value}", style = MaterialTheme.typography.headlineMedium)
    }
}