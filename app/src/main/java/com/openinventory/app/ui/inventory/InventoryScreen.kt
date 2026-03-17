package com.openinventory.app.ui.inventory

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel()
) {

    val barcode = viewModel.lastScan.collectAsState()

    Text(
        text = "Último código: ${barcode.value}"
    )
}