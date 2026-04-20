package com.openinventory.app.ui.comanda
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import androidx.compose.ui.platform.LocalContext

import java.util.*
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Busca Estilizada
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("O que vamos vender hoje?") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent)
            )

            // Lista de Produtos Estilizada
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.addToTempList(product.code, product.description, product.salePrice) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = colorResource(R.color.basic_purple).copy(alpha = 0.1f)) {
                                Icon(Icons.Default.AddCircle, null, Modifier.padding(8.dp), tint = colorResource(R.color.basic_purple))
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(product.description, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("R$ ${product.salePrice} | Est: ${product.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Checkout Panel (O Card de Finalização)
            if (tempItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 20.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("TOTAL DA VENDA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("R$ ${String.format("%.2f", tempItems.sumOf { it.third })}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = colorResource(R.color.basic_purple)))

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.quickSaleCustomerName,
                                onValueChange = { viewModel.quickSaleCustomerName = it },
                                label = { Text("Cliente", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.quickSaleCustomerCpf,
                                onValueChange = { viewModel.quickSaleCustomerCpf = it },
                                label = { Text("CPF", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.finishQuickSale { receiptText -> /* ... lógica do intent ... */ } },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.basic_purple)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, null)
                            Spacer(Modifier.width(8.dp))
                            Text("FINALIZAR E ENVIAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}