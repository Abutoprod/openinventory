package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.ui.scanner.ScannerScreen
import com.openinventory.app.ui.scanner.ScannerViewModel
import com.openinventory.app.ui.scanner.ScannerViewModelFactory // Vamos precisar disso
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import com.openinventory.app.ui.menu.MainMenu
import androidx.navigation.compose.composable
class MainActivity : ComponentActivity() {

    // Criamos o manager aqui para durar enquanto o app estiver aberto
    private lateinit var scannerManager: ScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerManager = ScannerManager(applicationContext)

        setContent {
            // 1. Criamos o NavController que controla a pilha de telas
            val navController = rememberNavController()

            // Usamos uma Factory para passar o scannerManager para dentro da ViewModel
            val viewModel: ScannerViewModel = viewModel(
                factory = ScannerViewModelFactory(scannerManager)
            )
            // 2. Definimos as rotas
            NavHost(navController = navController, startDestination = "main_menu") {
                // Rota do Menu Principal
                composable("main_menu") {
                    MainMenu(
                        onNavigateToScan = { navController.navigate("scanner") },
                        onNavigateToImport = { navController.navigate("import") },
                        onNavigateToHistory = { navController.navigate("history")}
                    )
                }
                // Rota do Scanner que você já criou
                composable("scanner") {
                    // Passamos o scannerManager e a viewModel para a sua ScannerScreen
                    ScannerScreen(viewModel, scannerManager)
                }
                // Rota do import ond evejo os produtos
                composable("import") {
                    // Passamos o scannerManager e a viewModel para a sua ScannerScreen
                    //ImportScreen(onBack = { navController.popBackStack() })
                }
                composable("history") {
                    // Tela de Histórico e Crítica
                    // HistoryScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}