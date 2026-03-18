package com.openinventory.app.ui.scanner

import android.content.IntentFilter
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // IMPORTANTE: Certifique-se de que é este import
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ScannerScreen(viewModel: ScannerViewModel, scannerManager: ScannerManager) {

    // Liga o scanner quando entra na tela e desliga ao sair
    DisposableEffect(Unit) {
        scannerManager.start()
        onDispose {
            scannerManager.stop()
        }
    }
    // Container Principal (Fundo Azul como na imagem)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)) // Cinza clarinho de fundo
    ) {
        // Cabeçalho Azul
        // Cabeçalho Azul
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF004AAD)), // Seu Azul

            verticalAlignment = Alignment.CenterVertically // Alinha ícone e texto no meio da altura
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ico),
                contentDescription = "Logo OpenInventory",
                modifier = Modifier.size(45.dp), // AQUI você controla o tamanho do logo
                tint = Color(0xFFFAF4F4) // Força a cor que você definiu
            )

            //Spacer(modifier = Modifier.width(10.dp)) // Dá um espaço entre o logo e o texto

            Text(
                text = "Open Inventory",
                color = Color.White,
                fontSize = 14.sp,               // Aumenta o tamanho da fonte
                fontWeight = FontWeight.ExtraBold, // Fonte com muito mais presença
                letterSpacing = 1.sp            // Um leve espaçamento entre letras para ar moderno
            )
        }

        // Área de Inputs (Cards Brancos)
        Column(modifier = Modifier.padding(16.dp)) {

            // Simulação do campo "Seção" da imagem
            InfoCard(label = "Seção", value = "1010")

            Spacer(modifier = Modifier.height(8.dp))

            // Último código lido em destaque
            val lastScan = viewModel.scannedProducts.firstOrNull()?.code ?: "Aguardando..."
            InfoCard(label = "Último Código lido", value = lastScan, isHighlight = true)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Produtos Lidos:",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // A LISTA ROLÁVEL (Substitui o RecyclerView)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(viewModel.scannedProducts) { product ->
                    ProductItem(product)
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
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight) Color(0xFF004AAD) else Color.Black
            )
        }
    }
}

@Composable
fun ProductItem(product: ScannedProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF004AAD))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = product.code, fontWeight = FontWeight.Medium)
                Text(text = "Lido em: ${product.timestamp}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

}