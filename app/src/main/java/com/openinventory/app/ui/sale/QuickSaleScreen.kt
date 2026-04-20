package com.openinventory.app.ui.comanda

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(viewModel: OrderViewModel, onBack: () -> Unit) {
    val tempItems by viewModel.tempItems.collectAsState()
    val inventoryProducts by viewModel.inventoryProducts.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, inventoryProducts) {
        inventoryProducts.filter { it.description.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Bar simples
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Venda Rápida", style = MaterialTheme.typography.headlineSmall)
        }

        // Busca
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar Produto...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Seleção (Estoque)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredProducts) { product ->
                ListItem(
                    headlineContent = { Text(product.description) },
                    supportingContent = { Text("R$ ${product.salePrice} | Est: ${product.quantity}") },
                    trailingContent = { Icon(Icons.Default.AddCircle, null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.clickable {
                        viewModel.addToTempList(product.code, product.description, product.salePrice)
                    }
                )
            }
        }

        // Card de Finalização (Seu código)
        if (tempItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Identificação (Opcional)", fontWeight = FontWeight.Bold)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = viewModel.quickSaleCustomerName,
                            onValueChange = { viewModel.quickSaleCustomerName = it },
                            label = { Text("Nome", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viewModel.quickSaleCustomerCpf,
                            onValueChange = { viewModel.quickSaleCustomerCpf = it },
                            label = { Text("CPF", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val total = tempItems.sumOf { it.third }
                    Button(
                        onClick = {
                            viewModel.finishQuickSale { receiptText ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, receiptText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Enviar Recibo"))
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("FECHAR VENDA (R$ ${String.format("%.2f", total)})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}