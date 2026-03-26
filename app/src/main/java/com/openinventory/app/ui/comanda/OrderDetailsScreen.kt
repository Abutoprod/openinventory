package com.openinventory.app.ui.comanda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    customerName: String,
    viewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val tempItems by viewModel.tempItems.collectAsState()
    val inventoryProducts by viewModel.inventoryProducts.collectAsState()
    val confirmedItems by viewModel.confirmedItems.collectAsState()

    // 1. Estado para o texto da busca
    var searchQuery by remember { mutableStateOf("") }

    // 2. Lógica de filtragem (Atualiza automaticamente quando searchQuery ou inventoryProducts mudam)
    val filteredProducts = remember(searchQuery, inventoryProducts) {
        if (searchQuery.isEmpty()) {
            inventoryProducts
        } else {
            inventoryProducts.filter { product ->
                product.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(orderId) {
        viewModel.loadConfirmedItems(orderId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Comanda: $customerName", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // 1. CAMPO DE BUSCA (Fixo no topo)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar produto...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. LISTA DE ESTOQUE (Ocupa o espaço principal)
        Text(
            "Estoque Disponível:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f) // <--- ISSO garante que ela divida o espaço e não suma com o resto
                .fillMaxWidth()
        ) {
            items(filteredProducts) { product ->
                ListItem(
                    headlineContent = { Text(product.description) },
                    supportingContent = { Text("R$ ${product.salePrice}") },
                    trailingContent = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Blue
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.addToTempList(
                            product.code,
                            product.description,
                            product.salePrice
                        )
                    }
                )
            }
        }

        // 3. SEÇÃO DO CARRINHO (Só aparece se houver itens)
        if (tempItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 2.dp)

            Text(
                "Itens Selecionados:",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFE91E63)
            )

            // LISTA DO CARRINHO
            LazyColumn(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(tempItems) { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${item.second}", modifier = Modifier.weight(1f))
                        Text("R$ ${item.third}", modifier = Modifier.padding(horizontal = 8.dp))

                        IconButton(onClick = { viewModel.removeFromTempList(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            } // <--- AQUI FECHA O LAZYCOLUMN DO CARRINHO

            // 4. BOTÃO DE CONFIRMAÇÃO (FICA FORA DA LISTA, MAS DENTRO DO IF)
            Button(
                onClick = {
                    viewModel.confirmOrderItems(orderId)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("CONFIRMAR LANÇAMENTO", fontWeight = FontWeight.Bold)
            }
        } // <--- AQUI FECHA O IF (TEMPITEMS.ISNOTEMPTY)
    } // <--- AQUI FECHA A COLUMN PRINCIPAL
} // <--- AQUI FECHA A FUNÇÃO ORDERDETAILSSCREEN