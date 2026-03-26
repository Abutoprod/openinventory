package com.openinventory.app.ui.comanda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // ESTE IMPORT É O SEGREDO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openinventory.app.data.database.entity.OrderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderViewModel,
    onOrderClick: (OrderEntity) -> Unit
) {
    // Coleta a lista do StateFlow do ViewModel
    val orders by viewModel.orders.collectAsState()

    // Chama a função para ouvir o Firebase
    LaunchedEffect(Unit) {
        viewModel.observeFirebaseOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comandas Ativas", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma comanda encontrada.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp)
            ) {
                // CORREÇÃO AQUI:
                // O 'items' abaixo agora usa corretamente a lista 'orders'
                items(
                    items = orders,
                    key = { it.orderId } // Ajuda o Compose a ser mais rápido
                ) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}