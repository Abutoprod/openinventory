package com.openinventory.app.ui.product
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // ESTE IMPORT É CRUCIAL
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.* import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.unit.dp
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Estados para os filtros (O nosso WHERE dinâmico)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TODOS") }
    val categories = listOf("TODOS", "BOOSTER", "EVENTO", "CONSUMIVEL")

    // Lógica de Filtragem (Equivalente ao LIKE %query% do SQL)
    val filteredProducts = products.filter { product ->
        val matchesQuery = product.description.contains(searchQuery, ignoreCase = true) ||
                product.code.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "TODOS" || product.category == selectedCategory

        matchesQuery && matchesCategory
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 8.dp) { // Dá profundidade ao cabeçalho
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("OPEN INVENTORY", fontWeight = androidx.compose.ui.text.font.FontWeight.Black) },
                        actions = {
                            IconButton(onClick = { viewModel.refreshFromFirebase() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync")
                            }
                        }
                    )

                    // Barra de Pesquisa (O LIKE '%%')
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("Pesquisar por nome ou SKU...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Seletor de Categorias (Chips)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo")
            }
        }
    ) { padding ->
        // ... (resto do código com o LazyColumn usando filteredProducts)
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espaçamento entre os cards
        ) {
            items(filteredProducts, key = { it.code }) { product ->
                ProductItemCard(
                    product = product,
                    onUpdateQty = { newQty -> viewModel.updateStock(product.code, newQty) }
                )
            }
        }

        if (showAddDialog) {
            AddProductDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { viewModel.saveNewProduct(it); showAddDialog = false }
            )
        }
    }
}