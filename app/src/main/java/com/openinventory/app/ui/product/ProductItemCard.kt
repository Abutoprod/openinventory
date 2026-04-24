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
        "BOOSTER" -> Color(0xFF9C27B0)
        "EVENTO" -> Color(0xFF2196F3)
        "PLAYMAT" -> Color(0xFFFF9800) // Adicionando cores para novas categorias
        "DECK" -> Color(0xFFE91E63)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp) // Reduzi levemente o padding interno
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de Categoria
            Surface(
                modifier = Modifier.size(44.dp), // Reduzi de 48 para 44
                shape = RoundedCornerShape(12.dp),
                color = categoryColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = if (isLowStock) MaterialTheme.colorScheme.error else categoryColor
                )
            }

            // Coluna de Texto com weight(1f) garante que ela ocupe o espaço disponível
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = product.description.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp, // Ajuste leve no tamanho
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color(0xFF1A1C1E),
                    maxLines = 1, // Travado em 1 linha para garantir o SKU e Preço visíveis
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "SKU: ${product.code}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "R$ ${String.format("%.2f", product.salePrice)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = colorResource(id = R.color.basic_purple),
                    maxLines = 1
                )
            }

            // Controle de Quantidade - Ajustado para números grandes
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF2F4F7),
                modifier = Modifier.widthIn(min = 90.dp) // Garante um tamanho mínimo
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (product.quantity > 0) onUpdateQty(product.quantity - 1) },
                        modifier = Modifier.size(32.dp) // Botão levemente menor
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = product.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (product.quantity > 999) 12.sp else 14.sp // Diminui a fonte se for > 1000
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        maxLines = 1
                    )

                    IconButton(
                        onClick = { onUpdateQty(product.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}