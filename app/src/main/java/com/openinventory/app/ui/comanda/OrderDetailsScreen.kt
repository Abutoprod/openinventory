package com.openinventory.app.ui.comanda

import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    customerName: String,
    viewModel: ComandaViewModel,
    onBack: () -> Unit
) {/*
    val tempItems by viewModel.tempItems.collectAsState()
    val inventoryProducts by viewModel.inventoryProducts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastReceiptText by remember { mutableStateOf("") }
    val filteredProducts = remember(searchQuery, inventoryProducts) {
        inventoryProducts.filter { it.description.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(orderId) { viewModel.loadConfirmedItems(orderId) }

    Scaffold(
        containerColor = Color(0xFFF2F4F7),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                Image(painter = painterResource(id = R.drawable.fundo), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), contentDescription = null)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colorResource(R.color.basic_purple).copy(alpha = 0.9f), colorResource(R.color.basic_purple).copy(alpha = 0.7f)))))
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Text("COMANDA: ${customerName.uppercase()}", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar produto...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Produtos
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredProducts) { product ->
                    ListItem(
                        headlineContent = { Text(product.description, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("R$ ${product.salePrice}") },
                        trailingContent = { Icon(Icons.Default.AddCircle, null, tint = colorResource(R.color.basic_purple)) },
                        modifier = Modifier.clickable { viewModel.addToTempList(product.code, product.description, product.salePrice) }
                    )
                }
            }

            // Carrinho Flutuante
            if (tempItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ITENS PARA LANÇAR", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            itemsIndexed(tempItems) { index, item ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.second, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                    IconButton(onClick = { viewModel.removeFromTempList(index) }) { Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.confirmOrderItems(orderId); onBack() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.basic_purple))
                        ) {
                            Text("CONFIRMAR LANÇAMENTO", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (showReceiptDialog) {
                ReceiptDialog(
                    receiptText = lastReceiptText,
                    onConfirm = {
                        // Lógica para partilhar via Intent
                        showReceiptDialog = false
                    },
                    onDismiss = { showReceiptDialog = false }
                )
            }
        }
        }*/
    }
