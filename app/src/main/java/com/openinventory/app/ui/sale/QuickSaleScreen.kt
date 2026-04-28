package com.openinventory.app.ui.comanda

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import ReceiptDialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(viewModel: OrderViewModel, onBack: () -> Unit) {
    val tempItems by viewModel.tempItems.collectAsState()
    val inventoryProducts by viewModel.inventoryProducts.collectAsState()
    val context = LocalContext.current

    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastReceiptText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, inventoryProducts) {
        inventoryProducts.filter { it.description.contains(searchQuery, ignoreCase = true) }
    }

    // ALERTA DE RECIBO (Isso vai aparecer ao finalizar)
    if (showReceiptDialog) {
        ReceiptDialog(
            receiptText = lastReceiptText,
            onConfirm = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, lastReceiptText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
                showReceiptDialog = false
                onBack() // Volta para o menu após partilhar
            },
            onDismiss = {
                showReceiptDialog = false
                onBack() // Também volta se apenas fechar
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F7),
        topBar = {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.fundo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(colorResource(R.color.orange_back).copy(alpha = 0.9f), colorResource(R.color.yellow_back).copy(alpha = 0.7f)))
                    ))
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                        Text("VENDA RÁPIDA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color.White))
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // BUSCA
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("O que vamos vender?") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent)
            )

            // LISTA DE PRODUTOS
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            viewModel.addToTempList(product.code, product.description, product.salePrice)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddCircle, null, tint = colorResource(R.color.basic_purple))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(product.description, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("R$ ${product.salePrice}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // PAINEL DE FECHAMENTO
            if (tempItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("TOTAL: R$ ${String.format("%.2f", tempItems.sumOf { it.third })}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = colorResource(R.color.basic_purple)))

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.finishQuickSale { receipt ->
                                    lastReceiptText = receipt
                                    showReceiptDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.basic_purple))
                        ) {
                            Text("FINALIZAR VENDA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}