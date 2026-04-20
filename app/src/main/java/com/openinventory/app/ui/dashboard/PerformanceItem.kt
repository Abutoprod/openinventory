package com.openinventory.app.ui.dashboard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
// Importamos o ProductStats para que o componente reconheça os dados
import com.openinventory.app.ui.dashboard.ProductStats
@Composable
fun PerformanceItem(product: ProductStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de progresso vertical colorida para dar um charme
            Box(Modifier.width(4.dp).height(30.dp).background(colorResource(R.color.basic_purple), CircleShape))

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = product.name.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "${product.quantity} vendidos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Text(
                text = "R$ ${String.format("%.2f", product.revenue)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = colorResource(R.color.basic_purple)
            )
        }
    }
}