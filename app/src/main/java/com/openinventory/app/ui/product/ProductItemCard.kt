package com.openinventory.app.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.openinventory.app.service.ProductResponse

@Composable
fun ProductItemCard(
    product: ProductResponse,
    modifier: Modifier = Modifier // Verifique se essa linha existe!
) {
    Card(
        modifier = modifier // E se ela é passada para o Card aqui!
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(product.code, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(product.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("R$ ${String.format("%.2f", product.price)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }

            // Badge de Quantidade
            Surface(
                color = if (product.quantity < 5) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${product.quantity} un",
                    Modifier.padding(8.dp),
                    color = if (product.quantity < 5) Color.Red else Color(0xFF2E7D32),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}