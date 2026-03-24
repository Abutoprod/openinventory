package com.openinventory.app.ui.scanner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.core.scanner.ScannerManager
import com.openinventory.app.core.scanner.ScannedProduct
import com.google.accompanist.permissions.isGranted
import androidx.compose.ui.platform.LocalContext
import com.openinventory.app.data.repository.ProductRepository

// Importe para CameraPreview (certifique-se que o pacote está correto conforme seu projeto)
// import com.openinventory.app.ui.scanner.CameraPreview

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel,
                  scannerManager: ScannerManager
                  )
{
    val ImpactFontFamily = FontFamily(
        Font(R.font.nautiluspompilius, FontWeight.Normal)
    )

    var isCameraOpen by remember { mutableStateOf(false) }

    val cameraPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    DisposableEffect(Unit) {
        scannerManager.start()
        onDispose { scannerManager.stop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6A1B9A)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ico),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Open Inventory",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ImpactFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Itens", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("${viewModel.scannedProducts.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val distinctCodes = viewModel.scannedProducts.distinctBy { it.code }.size
                            Text("SKUs Únicos", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("$distinctCodes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                InfoCard(label = "Seção", value = "1010")
                Spacer(modifier = Modifier.height(8.dp))
                val lastScan = viewModel.scannedProducts.firstOrNull()?.code ?: "Aguardando..."
                InfoCard(label = "Último Código lido", value = lastScan, isHighlight = true)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Produtos Lidos:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(items = viewModel.scannedProducts) { product ->
                        ProductItem(product = product, onDeleteConfirm = { viewModel.deleteProduct(product) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    isCameraOpen = true
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFF004AAD)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.diaphragm),
                contentDescription = "Câmera",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        if (isCameraOpen) {
            Box(modifier = Modifier.fillMaxSize()) {
                val context = LocalContext.current

                // AJUSTE AQUI: Mudamos de onProductScanned para onScan
                CameraPreview(onBarcodeScanned = { code ->
                    viewModel.buscarEProcessar(code) // <--- CORREÇÃO AQUI

                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(100)
                    }
                    isCameraOpen = false
                })

                CameraScannerOverlay()

                IconButton(
                    onClick = { isCameraOpen = false },
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Text(
                    text = "Posicione o código no centro",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
                )
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
            Text(value, style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight) Color(0xFF6A1B9A) else Color.Black)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItem(product: ScannedProduct, onDeleteConfirm: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Registro") },
            text = { Text("Deseja excluir o item: ${product.name}?") }, // Usando o nome para ficar mais claro
            confirmButton = {
                TextButton(onClick = { onDeleteConfirm(); showDeleteDialog = false }) {
                    Text("OK", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF6A1B9A))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                // EXIBE O NOME DO PRODUTO (que agora vem do banco)
                Text(text = product.name, fontWeight = FontWeight.Bold)
                Text(text = "SKU: ${product.code} • ${product.timestamp}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CameraScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val linePosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "line"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * linePosition
                drawLine(
                    color = Color.Red,
                    start = androidx.compose.ui.geometry.Offset(x = 20f, y = y),
                    end = androidx.compose.ui.geometry.Offset(x = size.width - 20f, y = y),
                    strokeWidth = 6f
                )
            }
        }
    }
}