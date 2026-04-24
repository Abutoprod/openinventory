package com.openinventory.app.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.core.config.CompanyConstants
import androidx.compose.foundation.clickable
import com.openinventory.app.ui.import.ImportSheet
import com.openinventory.app.ui.import.ImportViewModel

// --- COMPONENTE DE CARD PREMIUM (ModernMenuCard) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(
    onNavigateToScan: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToKits: () -> Unit,
    importViewModel: ImportViewModel,
    onNavigateToHistory: () -> Unit,
    currentStore: String, // Novo
    onStoreChange: (String) -> Unit // Novo
) {
    var showImportSheet by remember { mutableStateOf(false) }
    var showStoreMenu by remember { mutableStateOf(false) }
    val BackgroundColor = Color(0xFFF2F4F7)
    val PrimaryPurple = colorResource(R.color.orange_back)

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo e Título
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rayeart),
                                contentDescription = "Logo",
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "RAYEARTH",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }

                    // SEÇÃO DA FILIAL SELECIONADA
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { showStoreMenu = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Texto da Filial Atual
                            Text(
                                text = if (CompanyConstants.currentStoreId == "BAURU") "BAURU" else "MATRIZ",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = "Trocar Loja",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Texto pequeno de "Trocar"
                        Text(
                            text = "TROCAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                        )

                        DropdownMenu(
                            expanded = showStoreMenu,
                            onDismissRequest = { showStoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("MATRIZ") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                onClick = {
                                    onStoreChange("MATRIZ")
                                    showStoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("BAURU") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                onClick = {
                                    onStoreChange("BAURU")
                                    showStoreMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // Destaque: Comandas (Roxo)
            item {
                MainModernMenuCard(
                    title = "Comandas Ativas",
                    subtitle = "Gestão de mesas e consumo",
                    icon = Icons.Default.ConfirmationNumber,
                    containerColor = PrimaryPurple,
                    contentColor = Color.White,
                    onClick = onNavigateToSales
                )
            }

            // PDV e Métricas lado a lado
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "PDV",
                        subtitle = "Venda Rápida",
                        icon = Icons.Default.FlashOn,
                        onClick = onNavigateToKits
                    )
                    ModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Métricas",
                        subtitle = "Dashboard",
                        icon = Icons.Default.BarChart,
                        onClick = onNavigateToScan
                    )
                }
            }

            item {
                Text("GERENCIAMENTO", style = MaterialTheme.typography.labelLarge.copy(color = Color.Gray, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Estoque",
                        subtitle = "Cloud Sync",
                        icon = Icons.Default.Layers,
                        onClick = onNavigateToStock
                    )
                    ModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Importar",
                        subtitle = "Planilha CSV",
                        icon = Icons.Default.CloudUpload,
                        onClick = { showImportSheet = true }
                    )
                }
            }

            item {
                ModernMenuCard(
                    title = "Relatórios",
                    subtitle = "Auditoria e histórico",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToHistory
                )
            }
        }
    }

    if (showImportSheet) {
        ImportSheet(viewModel = importViewModel, onDismiss = { showImportSheet = false })
    }
}