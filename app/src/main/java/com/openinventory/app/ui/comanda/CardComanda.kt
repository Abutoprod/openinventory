// CardComanda.kt completo
package com.openinventory.app.ui.comanda

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.ComandaResponseDTO

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardComanda(
    comanda: ComandaResponseDTO,
    onAddClick: () -> Unit,
    onFecharClick: () -> Unit,
    onLongClick: () -> Unit // Callback para o clique longo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { /* Clique simples pode abrir detalhes também se quiser */ },
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = comanda.nomeCliente, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "R$ ${"%.2f".format(comanda.valorTotal)}",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (comanda.aberta) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (comanda.aberta) "ABERTA" else "FECHADA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (comanda.aberta) Color(0xFF2E7D32) else Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "${comanda.itens.size} itens", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.weight(1f),
                    enabled = comanda.aberta
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("LANÇAR", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onFecharClick,
                    modifier = Modifier.weight(1f),
                    enabled = comanda.aberta,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("FECHAR", fontSize = 12.sp)
                }
            }
        }
    }
}