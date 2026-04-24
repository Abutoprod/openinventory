package com.openinventory.app

import android.os.Bundle
import android.view.Window
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.openinventory.app.core.config.CompanyConstants
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
import com.openinventory.app.ui.viewmodel.ProductViewModel
import com.openinventory.app.ui.viewmodel.ProductViewModelFactory
import com.openinventory.app.ui.dashboard.DashboardViewModel
import com.openinventory.app.ui.dashboard.DashboardScreen
import com.openinventory.app.ui.comanda.OrderListScreen
import com.openinventory.app.ui.comanda.OrderViewModel
import com.openinventory.app.ui.comanda.OrderViewModelFactory
import com.openinventory.app.ui.comanda.OrderDetailsScreen
import com.openinventory.app.ui.comanda.QuickSaleScreen
import com.openinventory.app.ui.history.SalesHistoryScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mata a barra de título nativa (aquela bosta preta)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        actionBar?.hide()

        setContent {
            val navController = rememberNavController()

            val storeState = remember { mutableStateOf(CompanyConstants.currentStoreId) }

            // 1. INICIALIZAÇÃO DO BANCO E REPOSITÓRIOS (Ordem correta)
            val database = remember { AppDatabase.getDatabase(this) }
            val localDataSource = remember { LocalProductDataSource(database.productDao()) }
            val fileDataSource = remember { FileDataSource(applicationContext) }

            val productRepository = remember { ProductRepository(localDataSource, fileDataSource, database) }
            val orderRepository = remember { OrderRepository(database.orderDao()) }

            // 2. FACTORIES
            val productsFactory = ProductViewModelFactory(productRepository)
            val orderFactory = OrderViewModelFactory(orderRepository, productRepository,Firebase.firestore)

            // 3. VIEWMODELS COMPARTILHADOS (Criados uma única vez aqui no topo)
            // Isso garante que o 'init' não rode toda hora e economiza Firebase
            val sharedOrderViewModel: OrderViewModel = viewModel(factory = orderFactory)
            val sharedProductViewModel: ProductViewModel = viewModel(factory = productsFactory)

            NavHost(navController = navController, startDestination = "main_menu") {

                composable("main_menu") {
                    val importViewModel: ImportViewModel = viewModel(
                        factory = ImportViewModelFactory(productRepository)
                    )

                    MainMenu(
                        currentStore = storeState.value, // Passa o nome (MATRIZ/BAURU)
                        onStoreChange = { novaLoja ->
                            storeState.value = novaLoja
                            CompanyConstants.currentStoreId = novaLoja

                            // Atualiza as comandas (Firebase Direct)
                            sharedOrderViewModel.observeFirebaseOrders(novaLoja)

                            // Atualiza os produtos (Sync Room -> Firebase)
                            sharedProductViewModel.refreshFromFirebase()
                        },
                        onNavigateToScan = { navController.navigate("dashboard") },
                        onNavigateToStock = { navController.navigate("stock") },
                        onNavigateToSales = { navController.navigate("comandas") },
                        onNavigateToKits = { navController.navigate("pdv_rapido") },
                        onNavigateToHistory = { navController.navigate("history") },
                        importViewModel = importViewModel
                    )
                }

                composable("dashboard") {
                    val dashboardViewModel: DashboardViewModel = viewModel()
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("comandas") {
                    OrderListScreen(
                        viewModel = sharedOrderViewModel,
                        onOrderClick = { order ->
                            navController.navigate("order_details/${order.orderId}/${order.customerName}")
                        }
                    )
                }

                composable("order_details/{orderId}/{customerName}") {
                    OrderDetailsScreen(
                        orderId = it.arguments?.getString("orderId") ?: "",
                        customerName = it.arguments?.getString("customerName") ?: "",
                        viewModel = sharedOrderViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("stock") {
                    InventoryScreen(viewModel = sharedProductViewModel)
                }

                composable("pdv_rapido") {
                    QuickSaleScreen(
                        viewModel = sharedOrderViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("history") {
                    SalesHistoryScreen(
                        viewModel = sharedOrderViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}