package com.openinventory.app.ui.dashboard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openinventory.app.R
// Import importante para as legendas automáticas (FlowRow)
import androidx.compose.foundation.layout.ExperimentalLayoutApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val BackgroundColor = Color(0xFFF2F4F7)

    Scaffold(
        containerColor = BackgroundColor,
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Cards Principais de Faturamento
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "FATURAMENTO",
                        value = "R$ ${String.format("%.2f", uiState.totalRevenue)}",
                        icon = Icons.Default.Payments,
                        color = colorResource(R.color.basic_purple),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "LUCRO",
                        value = "R$ ${String.format("%.2f", uiState.totalProfit)}",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Gráfico Donut (Mais moderno que Pizza)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "DISTRIBUIÇÃO POR PRODUTO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (uiState.topProducts.isNotEmpty()) {
                            DashboardDonutChart(products = uiState.topProducts)
                        } else {
                            Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("Aguardando vendas...", color = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // 3. Ranking com PerformanceItem Refatorado
            item {
                Text(
                    text = "TOP PRODUTOS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
            }

            items(uiState.topProducts) { product ->
                PerformanceItem(product)
            }
        }
    }
}