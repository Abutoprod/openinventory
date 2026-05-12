package com.openinventory.app.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openinventory.app.R


@Composable
fun MainMenu(
    // Apenas o que realmente existe na MainActivity
    onNavigateToDashboard: () -> Unit,
    onNavigateToComandas: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToPdv: () -> Unit,
    onNavigateToHEvent: () -> Unit,
    currentStore: String,
    onStoreChange: (String) -> Unit,
    menuViewModel: MainMenuViewModel = viewModel()
) {
    var showImportSheet by remember { mutableStateOf(false) }
    var expandedStores by remember { mutableStateOf(false) }

    // Observa a lista de filiais da API
    val filiais by menuViewModel.filiais.collectAsState()

    // Encontra o nome da filial selecionada na lista que veio da API
    val selectedStoreName = filiais.find { it.id.toString() == currentStore }?.nome ?: "SELECIONAR FILIAL"

    Box(modifier = Modifier.fillMaxSize()) {
        // --- FUNDO PREMIUM ---
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.orange_back).copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(50.dp)) }

            // --- CABEÇALHO COM SELETOR DE FILIAL DA API ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OLÁ, ADMIN",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        // Botão que abre o menu de filiais
                        Row(
                            modifier = Modifier.clickable { expandedStores = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedStoreName.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                        }

                        // Menu Suspenso das Filiais
                        DropdownMenu(
                            expanded = expandedStores,
                            onDismissRequest = { expandedStores = false }
                        ) {
                            if (filiais.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Carregando filiais...") },
                                    onClick = { }
                                )
                            }

                            filiais.forEach { filial ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(filial.nome, fontWeight = FontWeight.Bold)
                                            Text(filial.cidade, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        onStoreChange(filial.id.toString()) // Salva o ID na memória (MainActivity)
                                        expandedStores = false
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.size(55.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rayeart),
                            contentDescription = "Logo",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // --- CARDS PRINCIPAIS (Ações rápidas) ---
            item {
                Text("OPERAÇÕES", style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold))
            }

            item {
                MainModernMenuCard(
                    title = "Dashboard",
                    subtitle = "Resumo de vendas e metas",
                    icon = Icons.Default.Analytics,
                    containerColor = colorResource(id = R.color.orange_back),
                    onClick = onNavigateToDashboard
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MainModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Comandas",
                        subtitle = "Gerenciar pedidos",
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToComandas
                    )
                    MainModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "PDV Rápido",
                        subtitle = "Venda direta",
                        icon = Icons.Default.ShoppingCart,
                        onClick = onNavigateToPdv
                    )
                }
            }

            // --- GERENCIAMENTO ---
            item {
                Text("SISTEMA", style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MainModernMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Estoque",
                        subtitle = "Cloud Sync",
                        icon = Icons.Default.Inventory,
                        onClick = onNavigateToStock
                    )

                }
            }

            item {
                MainModernMenuCard(
                    title = "Histórico de Vendas",
                    subtitle = "Auditoria completa",
                    icon = Icons.Default.History,
                    onClick = onNavigateToHEvent
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }


}