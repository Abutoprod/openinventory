package com.openinventory.app.ui.comanda

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.data.database.entity.OrderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderViewModel,
    onOrderClick: (OrderEntity) -> Unit
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val confirmedItems by viewModel.confirmedItems.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.observeFirebaseOrders()
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F7),
        topBar = {
            // TopBar com o degradê Laranja/Amarelo que você escolheu
            Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.fundo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.orange_back).copy(alpha = 0.9f),
                                colorResource(R.color.yellow_back).copy(alpha = 0.7f)
                            )
                        )
                    )
                )
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rayeart),
                            contentDescription = "Logo",
                            modifier = Modifier.size(54.dp).padding(0.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "RAYEARTH GAMES",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colorResource(R.color.basic_purple),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Comanda")
            }
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma comanda encontrada.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = orders, key = { it.orderId }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order) },
                        onLongClick = {
                            selectedOrder = order
                            viewModel.loadConfirmedItems(order.orderId)
                            showSheet = true
                        }
                    )
                }
            }
        }
    }

    // --- DIALOG NOVA COMANDA ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nova Comanda", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCustomerName,
                    onValueChange = { newCustomerName = it },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.basic_purple))
                ) { Text("Abrir") }
            }
        )
    }

    // --- SHEET DE RESUMO ---
    if (showSheet && selectedOrder != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(selectedOrder?.customerName?.uppercase() ?: "", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Total: R$ ${String.format("%.2f", selectedOrder?.totalAmount)}", color = colorResource(R.color.basic_purple), fontWeight = FontWeight.Bold)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(confirmedItems) { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.first, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            Text("R$ ${String.format("%.2f", item.second)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.finishOrderWithReceipt(selectedOrder!!) { receipt ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, receipt)
                            }
                            context.startActivity(Intent.createChooser(intent, "Enviar Recibo"))
                            showSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, null)
                    Spacer(Modifier.width(8.dp))
                    Text("FINALIZAR E GERAR RECIBO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}