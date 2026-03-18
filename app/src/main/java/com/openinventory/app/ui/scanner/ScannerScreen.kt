package com.openinventory.app.ui.scanner

import android.content.IntentFilter
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import com.openinventory.app.core.scanner.DataWedgeReceiver
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.R
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Box
import  androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Card
import com.openinventory.app.core.scanner.ScannedProduct
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.items // IMPORTANTE: Certifique-se de que é este import
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
@Composable
fun ScannerScreen(viewModel: ScannerViewModel, scannerManager: ScannerManager) {
    val ImpactFontFamily = FontFamily(
        Font(R.font.nautiluspompilius, FontWeight.Normal)
    )
    // Ciclo de vida: Liga o scanner ao entrar na tela e desliga ao sair
    DisposableEffect(Unit) {
        scannerManager.start()
        onDispose {
            scannerManager.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)) // Fundo cinza claro
    ) {
        // --- 1. CABEÇALHO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)),
                //.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ico),
                contentDescription = "Logo OpenInventory",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
            //Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Open Inventory",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ImpactFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // --- CONTEÚDO ROLÁVEL ---
        Column(modifier = Modifier.padding(16.dp)) {


            // --- BLOCO DE RESUMO LADO A LADO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Espaço entre os dois cards
            ) {
                // CARD 1: Total de Leituras
                Card(
                    modifier = Modifier.weight(1f), // Ocupa metade da largura
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Itens",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${viewModel.scannedProducts.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }

                // CARD 2: SKUs Únicos (ou outra métrica)
                Card(
                    modifier = Modifier.weight(1f), // Ocupa a outra metade
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Exemplo: contando quantos códigos diferentes foram lidos
                        val distinctCodes = viewModel.scannedProducts.distinctBy { it.code }.size

                        Text(
                            text = "SKUs Únicos",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "$distinctCodes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. INFO CARDS (SEÇÃO E ÚLTIMO LIDO) ---
            InfoCard(label = "Seção", value = "1010")

            Spacer(modifier = Modifier.height(8.dp))

            val lastScan = viewModel.scannedProducts.firstOrNull()?.code ?: "Aguardando..."
            InfoCard(label = "Último Código lido", value = lastScan, isHighlight = true)

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. TÍTULO DA LISTA ---
            Text(
                text = "Produtos Lidos:",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- 5. LISTA DE PRODUTOS ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = viewModel.scannedProducts) { product ->
                    ProductItem(
                        product = product,
                        onDeleteConfirm = {
                            viewModel.deleteProduct(product)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, isHighlight: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight) Color(0xFF6A1B9A) else Color.Black
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItem(
    product: ScannedProduct,
    onDeleteConfirm: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Diálogo de exclusão
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Registro") },
            text = { Text("Deseja excluir o código ${product.code} lido às ${product.timestamp}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirm()
                    showDeleteDialog = false
                }) {
                    Text("OK", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Ação de clique simples se desejar */ },
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (showDeleteDialog) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = if (showDeleteDialog) Color.Red else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = product.code, fontWeight = FontWeight.Medium)
                Text(
                    text = "Lido em: ${product.timestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}