package com.openinventory.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.ui.comanda.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(viewModel: OrderViewModel, onBack: () -> Unit) {
    // Aqui vamos buscar a coleção "sales" que criamos no processQuickSale
    val sales by viewModel.salesHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Vendas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.History, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (sales.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma venda registrada hoje.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales) { sale ->
                    SaleHistoryCard(sale)
                }
            }
        }
    }
}

@Composable
fun SaleHistoryCard(sale: SaleModel) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho (O que aparece fechado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.customerName.ifBlank { "Venda Rápida" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yy - HH:mm", Locale.getDefault()).format(sale.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "R$ ${String.format("%.2f", sale.total)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32),
                        fontSize = 18.sp
                    )
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation).padding(start = 8.dp)
                    )
                }
            }

            // Demonstrativo Expandido
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))

                    Text("DEMONSTRATIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    sale.items.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("1x", fontSize = 14.sp, color = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Text("R$ ${String.format("%.2f", item.price)}", fontSize = 14.sp)
                        }
                    }

                    if (sale.cpf.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("CPF: ${sale.cpf}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Modelo de dados simples para a lista
data class SaleModel(
    val customerName: String,
    val total: Double,
    val timestamp: Date,
    val items: List<SaleItem>,
    val cpf: String = ""
)

data class SaleItem(val name: String, val price: Double)