package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.data.repository.OrderRepository
import com.openinventory.app.ui.import.ImportViewModel
import com.openinventory.app.ui.import.ImportViewModelFactory
import com.openinventory.app.ui.menu.MainMenu
import com.openinventory.app.ui.product.InventoryScreen
import android.view.Window
import com.openinventory.app.ui.viewmodel.ProductViewModel
import com.openinventory.app.ui.viewmodel.ProductViewModelFactory
import com.openinventory.app.ui.dashboard.DashboardViewModel // Certifique-se que o pacote existe
import com.openinventory.app.ui.dashboard.DashboardScreen    // Sua nova tela
import com.openinventory.app.ui.comanda.OrderListScreen
import com.openinventory.app.ui.comanda.OrderViewModel
import com.openinventory.app.ui.comanda.OrderViewModelFactory
import com.openinventory.app.ui.comanda.OrderDetailsScreen
import com.openinventory.app.ui.comanda.QuickSaleScreen
import com.openinventory.app.ui.history.SalesHistoryScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// ISSO AQUI ESCONDE A BARRA DE STATUS E CABEÇALHOS DO SISTEMA
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        actionBar?.hide()

        setContent {
            val navController = rememberNavController()

            // 1. INICIALIZAÇÃO DO BANCO
            val database = remember { AppDatabase.getDatabase(this) }

            // 2. REPOSITÓRIOS
            val localDataSource = remember { LocalProductDataSource(database.productDao()) }
            val fileDataSource = remember { FileDataSource(applicationContext) }
            val productRepository = remember { ProductRepository(localDataSource, fileDataSource, database) }
            val orderRepository = remember { OrderRepository(database.orderDao()) }

            // 3. FACTORIES
            val productsFactory = ProductViewModelFactory(productRepository)
            val orderFactory = OrderViewModelFactory(orderRepository, productRepository)

            NavHost(navController = navController, startDestination = "main_menu") {

                composable("main_menu") {
                    val importViewModel: ImportViewModel = viewModel(
                        factory = ImportViewModelFactory(productRepository)
                    )

                    MainMenu(
                        // Agora o botão de Scan vira o de Dashboard!
                        onNavigateToScan = { navController.navigate("dashboard") },
                        onNavigateToStock = { navController.navigate("stock") },
                        onNavigateToSales = { navController.navigate("comandas") },
                        onNavigateToKits = { navController.navigate("pdv_rapido") },
                        onNavigateToHistory = { navController.navigate("history") },
                        importViewModel = importViewModel
                    )
                }

                // TELA DE DASHBOARD (Substituindo a antiga Dash/Scanner)
                composable("dashboard") {
                    // Se o seu DashboardViewModel não precisa de parâmetros no construtor,
                    // você nem precisa de Factory, o Compose cria sozinho:
                    val dashboardViewModel: DashboardViewModel = viewModel()
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // ... (restante das rotas: comandas, stock, etc permanecem iguais)
                composable("comandas") {
                    val comandaViewModel: OrderViewModel = viewModel(factory = orderFactory)
                    OrderListScreen(
                        viewModel = comandaViewModel,
                        onOrderClick = { order ->
                            navController.navigate("order_details/${order.orderId}/${order.customerName}")
                        }
                    )
                }

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
                    QuickSaleScreen(viewModel = comandaViewModel, onBack = { navController.popBackStack() })
                }

                composable("history") {
                    val saleViewModel: OrderViewModel = viewModel(factory = orderFactory)
                    SalesHistoryScreen(viewModel = saleViewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}