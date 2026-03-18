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
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
@Composable
fun ScannerScreen(viewModel: ScannerViewModel, scannerManager: ScannerManager) {

    // Liga o scanner quando entra na tela e desliga ao sair
    DisposableEffect(Unit) {
        scannerManager.start()
        onDispose {
            scannerManager.stop()
        }
    }
    // Estrutura Visual (O equivalente ao seu antigo XML)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Leitor de Inventário",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // O texto muda sozinho quando o viewModel.scanResult for atualizado!
        Text(
            text = if (viewModel.scanResult.value.isEmpty())
                "Aguardando bipo..."
            else
                "Código: ${viewModel.scanResult.value}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}