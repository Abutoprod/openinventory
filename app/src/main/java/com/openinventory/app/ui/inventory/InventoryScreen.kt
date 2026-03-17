package com.openinventory.app.ui.inventory

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.openinventory.app.ui.scanner.ScannerViewModel

@Composable
fun InventoryScreen(viewModel: ScannerViewModel) {

    val barcode = viewModel.scanResult.value

    Text(
        text = "Último código: ${barcode}"
    )
}