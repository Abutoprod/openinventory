package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.ui.scanner.ScannerScreen
import com.openinventory.app.ui.scanner.ScannerViewModel
import com.openinventory.app.ui.scanner.ScannerViewModelFactory // Vamos precisar disso

class MainActivity : ComponentActivity() {

    // Criamos o manager aqui para durar enquanto o app estiver aberto
    private lateinit var scannerManager: ScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerManager = ScannerManager(applicationContext)

        setContent {
            // Usamos uma Factory para passar o scannerManager para dentro da ViewModel
            val viewModel: ScannerViewModel = viewModel(
                factory = ScannerViewModelFactory(scannerManager)
            )

            // Passamos o manager para a Screen para ela controlar o Start/Stop
            ScannerScreen(viewModel, scannerManager)
        }
    }
}