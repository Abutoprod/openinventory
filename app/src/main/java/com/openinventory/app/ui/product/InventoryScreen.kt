package com.openinventory.app.ui.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.service.ProductResponse
import com.openinventory.app.ui.product.AddProductSheet
import com.openinventory.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(viewModel: ProductViewModel, currentStoreId: String) {
    // Observando os estados do ViewModel
    val products by viewModel.products.collectAsState()
    val storeName by viewModel.currentStoreName.collectAsState()

    // Estados para controlar o Modal
    var showSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductResponse?>(null) }

    // Sincroniza a filial ao abrir a tela
    LaunchedEffect(currentStoreId) {
        viewModel.updateStore(currentStoreId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(id = R.color.orange_back).copy(alpha = 0.1f),
                border = BorderStroke(1.dp, colorResource(id = R.color.orange_back).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = colorResource(id = R.color.orange_back))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("ESTOQUE ATUAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(storeName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null // Garante que é um novo cadastro
                    showSheet = true
                },
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (products.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum produto encontrado nesta filial.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Mais espaço entre os itens
                ) {
                    items(products) { product ->
                        // USAMOS O MODIFIER AQUI PARA O CLIQUE LONGO
                        ProductItemCard(
                            product = product,
                            modifier = Modifier.combinedClickable(
                                onClick = { /* Clique normal: se quiser abrir algo */ },
                                onLongClick = {
                                    productToEdit = product // Define o produto para edição
                                    showSheet = true // Abre o modal
                                }
                            )
                        )
                    }
                }
            }

            // MODAL (AddProductSheet)
            if (showSheet) {
                AddProductSheet(
                    currentStoreId = currentStoreId,
                    productToEdit = productToEdit, // Passa o produto se for edição, ou null se for novo
                    onDismiss = {
                        showSheet = false
                        productToEdit = null
                    },
                    // Dentro de InventoryScreen.kt -> AddProductSheet
                    onConfirm = { dto ->
                        val produtoAtual = productToEdit
                        if (produtoAtual != null) {
                            // Agora o 'id' existe no ProductResponse!
                            viewModel.alterarProduto(produtoAtual.id, dto)
                        } else {
                            viewModel.salvarProduto(dto)
                        }
                        showSheet = false
                        productToEdit = null
                    }
                )
            }
        }
    }
}