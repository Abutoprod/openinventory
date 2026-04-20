package com.openinventory.app.ui.dashboard
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R

// Import essencial para as legendas (FlowRow)
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardDonutChart(products: List<ProductStats>) {
    val totalRevenue = products.sumOf { it.revenue }.toFloat()
    val colors = listOf(colorResource(R.color.basic_purple), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF00BCD4), Color(0xFFE91E63))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                products.forEachIndexed { index, product ->
                    val sweepAngle = (product.revenue.toFloat() / totalRevenue) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false, // Aqui vira Donut
                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
            // Texto no centro do Donut
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    "R$ ${String.format("%.0f", totalRevenue)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legendas responsivas
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            products.take(6).forEachIndexed { index, product ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                    Box(Modifier.size(10.dp).background(colors[index % colors.size], CircleShape))
                    Text(" ${product.name}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}