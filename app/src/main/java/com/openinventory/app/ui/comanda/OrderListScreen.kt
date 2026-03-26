package com.openinventory.app.ui.comanda

import androidx.compose.material.icons.Icons // IMPORTANTE para clique longo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openinventory.app.data.database.entity.OrderEntity
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderViewModel,
    onOrderClick: (OrderEntity) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val confirmedItems by viewModel.confirmedItems.collectAsState()

    // Estados para o Demonstrativo (Sheet)
    var showSheet by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Estados para o NOVO DIALOG de Criar Comanda
    var showDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.observeFirebaseOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comandas Ativas", fontWeight = FontWeight.Bold) }
            )
        },// --- ADICIONANDO O BOTÃO FLUTUANTE AQUI ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Comanda")
            }
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(
                    items = orders,
                    key = { it.orderId }
                ) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order) },
                        // --- AQUI ESTÁ A NOVIDADE ---
                        onLongClick = {
                            selectedOrder = order
                            viewModel.loadConfirmedItems(order.orderId) // Busca os itens no Firebase
                            showSheet = true
                        }
                    )
                }
            }
        }
    }
// --- DIALOG PARA CRIAR NOVA COMANDA ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nova Comanda") },
            text = {
                OutlinedTextField(
                    value = newCustomerName,
                    onValueChange = { newCustomerName = it },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerName.isNotBlank()) {
                            viewModel.createNewOrder(newCustomerName)
                            newCustomerName = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Abrir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    // --- BLOCO DO DEMONSTRATIVO (Fica fora do Scaffold) ---
    if (showSheet && selectedOrder != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Resumo: ${selectedOrder?.customerName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Total Acumulado: R$ ${String.format("%.2f", selectedOrder?.totalAmount)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Produtos Consumidos:", style = MaterialTheme.typography.labelLarge)

                // LISTA DE ITENS DENTRO DO SHEET
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp) // Não deixa o sheet ocupar a tela toda
                ) {
                    items(confirmedItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${item.first}", modifier = Modifier.weight(1f))
                            Text("R$ ${String.format("%.2f", item.second)}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÃO FINALIZAR COMANDA
                Button(
                    onClick = {
                        viewModel.finishOrder(selectedOrder!!.orderId)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Vermelho
                ) {
                    Text("FINALIZAR E FECHAR CONTA", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}