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
import androidx.compose.foundation.BorderStroke
@Composable
fun ProductItemCard(
    product: ProductResponse,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp), // Cantos mais arredondados
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LADO ESQUERDO: Informações do Produto
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = product.code,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.description,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preço com fundo sutil
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "R$ ${"%.2f".format(product.price)}",
                        color = Color(0xFF059669), // Verde esmeralda moderno
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            // LADO DIREITO: Badge de Estoque Grande e Profissional
            val estoqueBaixo = product.quantity < 10
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Surface(
                    color = if (estoqueBaixo) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (estoqueBaixo) Color(0xFFFCA5A5) else Color(0xFFA7F3D0)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${product.quantity}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (estoqueBaixo) Color(0xFFB91C1C) else Color(0xFF065F46)
                        )
                        Text(
                            text = "ESTOQUE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (estoqueBaixo) Color(0xFFB91C1C) else Color(0xFF065F46)
                        )
                    }
                }
            }
        }
    }
}