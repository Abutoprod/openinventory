package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.data.repository.OrderRepository
import com.openinventory.app.ui.import.ImportViewModel
import com.openinventory.app.ui.import.ImportViewModelFactory
import com.openinventory.app.ui.menu.MainMenu
import com.openinventory.app.ui.product.InventoryScreen
import com.openinventory.app.ui.viewmodel.ProductViewModel
import com.openinventory.app.ui.viewmodel.ProductViewModelFactory
import com.openinventory.app.ui.scanner.ScannerScreen
import com.openinventory.app.ui.scanner.ScannerViewModel
import com.openinventory.app.ui.scanner.ScannerViewModelFactory
import com.openinventory.app.ui.comanda.OrderListScreen
import com.openinventory.app.ui.comanda.OrderViewModel
import com.openinventory.app.ui.comanda.OrderViewModelFactory
import com.openinventory.app.ui.comanda.OrderDetailsScreen
import com.openinventory.app.ui.comanda.QuickSaleScreen
import com.openinventory.app.ui.history.SalesHistoryScreen

class MainActivity : ComponentActivity() {

    private lateinit var scannerManager: ScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerManager = ScannerManager(applicationContext)

        setContent {
            val navController = rememberNavController()

            // 1. INICIALIZAÇÃO DO BANCO (O "DBA" do App)
            val database = remember { AppDatabase.getDatabase(this) }

            // 2. REPOSITÓRIOS (A ponte entre o Banco e a UI)
            val localDataSource = remember { LocalProductDataSource(database.productDao()) }
            val fileDataSource = remember { FileDataSource(applicationContext) }
            val productRepository = remember { ProductRepository(localDataSource, fileDataSource, database) }

            // CORREÇÃO AQUI: Passamos o database.orderDao() para o repositório
            val orderRepository = remember { OrderRepository(database.orderDao()) }
            // 3. FACTORIES (As "máquinas" que criam ViewModels)
            val scannerFactory = ScannerViewModelFactory(scannerManager, productRepository)
            val productsFactory = ProductViewModelFactory(productRepository)
            val orderFactory = OrderViewModelFactory(orderRepository,productRepository)

            NavHost(navController = navController, startDestination = "main_menu") {

                composable("main_menu") {
                    val importViewModel: ImportViewModel = viewModel(
                        factory = ImportViewModelFactory(productRepository)
                    )

                    MainMenu(
                        onNavigateToScan = { navController.navigate("scanner") },
                        onNavigateToStock = { navController.navigate("stock") },
                        onNavigateToSales = { navController.navigate("comandas") },
                        onNavigateToKits = { navController.navigate("pdv_rapido") },
                        onNavigateToHistory = { navController.navigate("history") },
                        importViewModel = importViewModel
                    )
                }

                composable("scanner") {
                    val scannerViewModel: ScannerViewModel = viewModel(factory = scannerFactory)
                    ScannerScreen(viewModel = scannerViewModel, scannerManager = scannerManager)
                }

                composable("comandas") {
                    val comandaViewModel: OrderViewModel = viewModel(factory = orderFactory)

                    OrderListScreen(
                        viewModel = comandaViewModel,
                        onOrderClick = { order ->
                            // Ao clicar no card, mandamos o ID e o Nome pela rota
                            navController.navigate("order_details/${order.orderId}/${order.customerName}")
                        }
                    )
                }
                // NOVA ROTA: Tela de Detalhes (onde você vai add os produtos)
                composable("order_details/{orderId}/{customerName}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
                    val comandaViewModel: OrderViewModel = viewModel(factory = orderFactory)

                    OrderDetailsScreen(
                        orderId = orderId,
                        customerName = customerName,
                        viewModel = comandaViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("stock") {
                    val productViewModel: ProductViewModel = viewModel(factory = productsFactory)
                    InventoryScreen(viewModel = productViewModel)
                }

                composable("pdv_rapido") {
                    val comandaViewModel: OrderViewModel = viewModel(factory = orderFactory)
                    QuickSaleScreen(
                        viewModel = comandaViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("history") {
                    val saleViewModel: OrderViewModel = viewModel(factory = orderFactory)
                    SalesHistoryScreen(
                        viewModel = saleViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}