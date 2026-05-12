package com.openinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openinventory.app.core.config.CompanyConstants
import com.openinventory.app.core.config.RetrofitClient
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.ui.menu.MainMenu
import com.openinventory.app.ui.product.InventoryScreen
import com.openinventory.app.ui.viewmodel.ProductViewModel
import com.openinventory.app.ui.viewmodel.ProductViewModelFactory
import com.openinventory.app.ui.login.LoginScreen
import com.openinventory.app.ui.dashboard.DashboardScreen
import com.openinventory.app.data.repository.PontosRepository
import com.openinventory.app.ui.ponto.PontosViewModel
import com.openinventory.app.ui.ponto.RankingScreen
// Novos Imports para Comandas
import com.openinventory.app.ui.comanda.ComandaScreen
import com.openinventory.app.ui.comanda.ComandaViewModel
import com.openinventory.app.data.repository.ComandaRepository
import com.openinventory.app.ui.sale.VendaRapidaViewModel
import com.openinventory.app.ui.sale.VendaRapidaScreen
import com.openinventory.app.data.repository.DashboardRepository
import com.openinventory.app.ui.dashboard.DashboardViewModel
import com.openinventory.app.ui.eventos.CadastroEventoScreen
import com.openinventory.app.ui.eventos.EventoViewModel
import com.openinventory.app.data.repository.EventoRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitClient.instance
        val productRepository = ProductRepository(apiService)

        setContent {
            val navController = rememberNavController()
            var currentStore by remember { mutableStateOf(CompanyConstants.currentStoreId) }

            // 1. Inicializa o ViewModel do Estoque (Shared)
            val sharedProductViewModel: ProductViewModel = viewModel(
                factory = ProductViewModelFactory(productRepository)
            )

            // 2. Inicializa o ViewModel das Comandas (Shared)
            // Declarado aqui em cima para ser visível em todo o NavHost
            val comandaRepository = ComandaRepository(apiService)
            val comandaViewModel = ComandaViewModel(comandaRepository)
            val vendaRapidaViewModel = VendaRapidaViewModel(comandaRepository)
            val dashboardRepository = DashboardRepository(apiService) // NOVO
            val dashboardViewModel = DashboardViewModel(dashboardRepository) // NOVO
            val eventoRepository = EventoRepository(apiService)
            val eventoViewModel = EventoViewModel(eventoRepository)

            val pontosRepository = PontosRepository(apiService)
            val pontosViewModel = PontosViewModel(pontosRepository,eventoRepository)

            NavHost(navController = navController, startDestination = "login") {

                composable("login") {
                    LoginScreen(
                        onNavigateToMenu = {
                            navController.navigate("main_menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable("main_menu") {
                    MainMenu(
                        onNavigateToDashboard = { navController.navigate("dashboard") },
                        onNavigateToComandas = { navController.navigate("comandas") },
                        onNavigateToStock = { navController.navigate("stock") },
                        onNavigateToHEvent = { navController.navigate("eventos")},
                        onNavigateToPdv = { navController.navigate("venda_rapida") },
                        onNavigateToScore = { navController.navigate("pontos") },
                        currentStore = currentStore,
                        onStoreChange = { newStore ->
                            currentStore = newStore
                            sharedProductViewModel.updateStore(newStore)
                        }
                    )
                }

                composable("dashboard") {
                    // Chamamos a nova tela passando o ID da filial selecionada
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        filialId = currentStore.toLongOrNull() ?: 0L
                    )
                }
                // NOVO: Rota para Cadastro de Eventos
                composable("eventos") {
                    val filialId = currentStore.toLongOrNull() ?: 0L
                    CadastroEventoScreen(
                        viewModel = eventoViewModel,
                        filialId = filialId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("pontos") {
                    val filialId = currentStore.toLongOrNull() ?: 0L

                    // ADICIONE ESTA LINHA: Sem ela, o ViewModel nunca busca jogos ou clientes
                    LaunchedEffect(filialId) {
                        pontosViewModel.inicializar(filialId)
                    }

                    RankingScreen(
                        viewModel = pontosViewModel,
                        filialId = filialId
                    )
                }

                composable("comandas") {
                    ComandaScreen(
                        viewModel = comandaViewModel,
                        currentStoreId = currentStore
                    )
                }
                composable("venda_rapida") {
                    val filialId = currentStore.toLongOrNull() ?: 0L

                    // Dispara o carregamento assim que a rota é acessada
                    LaunchedEffect(filialId) {
                        vendaRapidaViewModel.carregarProdutos(filialId)
                    }

                    val produtosLoja by vendaRapidaViewModel.produtosLoja.collectAsState()
                    val isLoading by vendaRapidaViewModel.isLoading.collectAsState()

                    VendaRapidaScreen(
                        viewModel = vendaRapidaViewModel,
                        filialId = filialId,
                        produtosLoja = produtosLoja // Agora vem do próprio ViewModel de venda
                    )
                }

                composable("stock") {
                    // Passamos o currentStore (ID selecionado no menu) para a tela
                    InventoryScreen(
                        viewModel = sharedProductViewModel,
                        currentStoreId = currentStore
                    )
                }

            }
        }
    }
}