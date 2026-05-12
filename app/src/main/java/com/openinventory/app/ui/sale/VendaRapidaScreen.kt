package com.openinventory.app.ui.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openinventory.app.service.ProductResponse
import com.openinventory.app.ui.comanda.AdicionarProdutoDialog
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendaRapidaScreen(
    viewModel: VendaRapidaViewModel,
    filialId: Long,
    produtosLoja: List<ProductResponse>
) {
    val carrinho by viewModel.carrinho.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddProdutoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Venda Rápida (Cliente 99999)") }) },
        floatingActionButton = {
            if (carrinho.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.confirmarVenda(filialId) {
                            scope.launch { snackbarHostState.showSnackbar("Venda Finalizada!") }
                        }
                    },
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text("Confirmar") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(
                onClick = { showAddProdutoDialog = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Adicionar Produto")
            }

            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            LazyColumn {
                items(carrinho) { item ->
                    ListItem(
                        headlineContent = { Text(item.produto.description) },
                        supportingContent = { Text("Código: ${item.produto.code} | Qtd: ${item.quantidade}") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.removerOuDiminuir(item.produto) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, null)
                                }
                                Text("${item.quantidade}")
                                IconButton(onClick = { viewModel.adicionarAoCarrinho(item.produto) }) {
                                    Icon(Icons.Default.AddCircleOutline, null)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddProdutoDialog) {
        AdicionarProdutoDialog(
            produtosDisponiveis = produtosLoja,
            onDismiss = { showAddProdutoDialog = false },
            onConfirm = { produto, _ ->
                viewModel.adicionarAoCarrinho(produto)
                showAddProdutoDialog = false
            }
        )
    }
}