package com.openinventory.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.ui.import.ImportSheet
import com.openinventory.app.ui.import.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(
    onNavigateToScan: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToSales: () -> Unit, // Agora mapeado para 'comandas'
    onNavigateToKits: () -> Unit,  // Agora mapeado para 'pdv_rapido'
    importViewModel: ImportViewModel,
    onNavigateToHistory: () -> Unit
) {
    var showImportSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // Pode ser conectado ao ViewModel depois

    val ImpactFontFamily = FontFamily.Default

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.basic_purple))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ico),
                        contentDescription = "Logo",
                        modifier = Modifier.size(45.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Open Inventory - TCG",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = ImpactFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA)) // Fundo mais clean
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- SEÇÃO 1: OPERAÇÃO COMERCIAL (PRIORIDADE ALTA) ---

                // Botão de Comandas - O coração da loja
                MenuActionCard(
                    title = "Comandas Ativas",
                    description = "Gerenciar mesas e consumo dos players",
                    icon = Icons.Default.Assignment,
                    containerColor = colorResource(R.color.basic_purple),
                    iconColor = Color.White,
                    onClick = onNavigateToSales
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Venda Rápida",
                        subtitle = "Balcão (Boosters/Acessórios)",
                        icon = Icons.Default.ShoppingCart,
                        onClick = onNavigateToKits
                    )
                    SecondaryMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Nova Coleta",
                        subtitle = "Scanner de Inventário",
                        icon = Icons.Default.CameraAlt,
                        onClick = onNavigateToScan
                    )
                }

                // --- SEÇÃO 2: GESTÃO E LOGÍSTICA ---

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Estoque Cloud",
                        subtitle = "Saldos e Cards",
                        icon = Icons.Default.Inventory,
                        onClick = onNavigateToStock
                    )
                    SecondaryMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Importar CSV",
                        subtitle = "Catálogo da Loja",
                        icon = Icons.Default.UploadFile,
                        onClick = { showImportSheet = true }
                    )
                }

                // --- SEÇÃO 3: RELATÓRIOS ---

                MenuActionCard(
                    title = "Histórico & Relatórios",
                    description = "Ver seções salvas e fechamento de caixa",
                    icon = Icons.Default.History,
                    containerColor = Color.White,
                    iconColor = colorResource(R.color.basic_purple),
                    onClick = onNavigateToHistory
                )

                Spacer(modifier = Modifier.weight(1f))

                // Rodapé com versão
                Text(
                    text = "v2.0.0 Cloud - Chão Sistemas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    letterSpacing = 1.sp
                )
            }
        }

        // Overlay de Loading
        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sincronizando Dados...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showImportSheet) {
        ImportSheet(
            viewModel = importViewModel,
            onDismiss = { showImportSheet = false }
        )
    }
}


