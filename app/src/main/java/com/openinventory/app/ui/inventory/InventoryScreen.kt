package com.openinventory.app.ui.inventory

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.openinventory.app.core.scanner.ScannerManager
import androidx.compose.runtime.DisposableEffect
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel()
) {

    val context = LocalContext.current
    val scanner = remember { ScannerManager(context) }

    DisposableEffect(Unit) {

        scanner.setListener {
            viewModel.onScan(it.barcode)
        }

        scanner.startScanner()

        onDispose {
            scanner.stopScanner()
        }
    }

    val barcode = viewModel.lastScan.collectAsState()

    Text(
        text = "Último código: ${barcode.value}"
    )
}