package com.openinventory.app.ui.product
import androidx.compose.runtime.Composable
import com.openinventory.app.data.database.entity.ProductEntity
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
@Composable
fun ProductItemCard(product: ProductEntity, onUpdateQty: (Int) -> Unit) {
    val isLowStock = product.quantity < 5
    val categoryColor = when (product.category) {
        "BOOSTER" -> Color(0xFF9C27B0) // Roxo
        "EVENTO" -> Color(0xFF2196F3)  // Azul
        else -> Color(0xFF4CAF50)      // Verde para Consumíveis
    }
    // Definimos a cor do card: se baixo, um tom de vermelho suave, se não, a cor padrão
    val cardColor = if (isLowStock) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            // Se o estoque estiver baixo, fica vermelho, senão usa a cor da categoria bem suave
            containerColor = if (product.quantity < 5) MaterialTheme.colorScheme.errorContainer
            else categoryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(4.dp).height(40.dp).background(categoryColor))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.description.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(text = "SKU: ${product.code}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "R$ ${String.format("%.2f", product.salePrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                )
            }

            // Controle de Quantidade
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQty(product.quantity - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos")
                    }
                    Text(
                        text = product.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { onUpdateQty(product.quantity + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Mais")
                    }
                }
            }
        }
    }
}