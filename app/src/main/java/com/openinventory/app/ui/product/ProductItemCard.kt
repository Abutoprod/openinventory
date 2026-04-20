package com.openinventory.app.ui.product
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R
import com.openinventory.app.data.database.entity.ProductEntity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun ProductItemCard(product: ProductEntity, onUpdateQty: (Int) -> Unit) {
    val isLowStock = product.quantity < 5
    val categoryColor = when (product.category) {
        "BOOSTER" -> Color(0xFF9C27B0) // Roxo
        "EVENTO" -> Color(0xFF2196F3)  // Azul
        else -> Color(0xFF4CAF50)      // Verde
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Padding lateral reduzido para usar o do LazyColumn
        shape = RoundedCornerShape(24.dp), // Mantendo o padrão do Main Menu
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de Categoria Estilizado
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = categoryColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (isLowStock) MaterialTheme.colorScheme.error else categoryColor
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = product.description.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp, // Tamanho fixo menor para evitar quebras feias
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color(0xFF1A1C1E),
                    maxLines = 2, // Permite no máximo 2 linhas se o nome for gigante
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "SKU: ${product.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "R$ ${String.format("%.2f", product.salePrice)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp // Preço em destaque mas controlado
                    ),
                    color = colorResource(id = R.color.basic_purple),
                    maxLines = 1
                )
            }
            // Controle de Quantidade Moderno
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF2F4F7),
                modifier = Modifier.wrapContentWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQty(product.quantity - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = product.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { onUpdateQty(product.quantity + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}