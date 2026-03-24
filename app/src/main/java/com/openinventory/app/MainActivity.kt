package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.ui.import.ImportViewModel
import com.openinventory.app.ui.import.ImportViewModelFactory
import com.openinventory.app.ui.menu.MainMenu
import com.openinventory.app.ui.scanner.ScannerScreen
import com.openinventory.app.ui.scanner.ScannerViewModel
import com.openinventory.app.ui.scanner.ScannerViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var scannerManager: ScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerManager = ScannerManager(applicationContext)

        setContent {
            val navController = rememberNavController()

            // 1. PRIMEIRO inicializamos o banco e o repositório
            val database = AppDatabase.getDatabase(this) // Ou como você inicializa seu Room
            val localDataSource = LocalProductDataSource(database.productDao())
            val fileDataSource = FileDataSource(applicationContext)
            val repository = ProductRepository(localDataSource, fileDataSource, database)


            // 2. DEPOIS criamos a factory usando o repository que já existe acima
            val scannerFactory = ScannerViewModelFactory(scannerManager, repository)

            NavHost(navController = navController, startDestination = "main_menu") {

                composable("main_menu") {
                    // Aqui usamos a factory de importação
                    val importViewModel: ImportViewModel = viewModel(
                        factory = ImportViewModelFactory(repository)
                    )

                    MainMenu(
                        onNavigateToScan = { navController.navigate("scanner") },
                        onNavigateToHistory = { navController.navigate("history") },
                        importViewModel = importViewModel
                    )
                }

                composable("scanner") {
                    // 3. AGORA usamos a scannerFactory que criamos ali no passo 2
                    val scannerViewModel: ScannerViewModel = viewModel(
                        factory = scannerFactory
                    )

                    ScannerScreen(
                        viewModel = scannerViewModel,
                        scannerManager = scannerManager
                    )
                }

                composable("history") {
                    // HistoryScreen(...)
                }
            }
        }
    }
}