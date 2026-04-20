package com.openinventory.app.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainModernMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF1A1C1E),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container do Ícone (Fixo, não deforma)
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (containerColor == Color.White) contentColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (containerColor == colorResource(id = R.color.white)) {
                        contentColor
                    } else {
                        colorResource(id = R.color.white)
                    } )
            }

            // A MUDANÇA ESTÁ AQUI: weight(1f) e padding
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f) // Preenche o espaço restante sem deformar o card
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 18.sp // Ajuste leve para ser mais responsivo
                    ),
                    color = if (containerColor == Color.White) contentColor else Color.White,
                    maxLines = 1, // No menu, geralmente 1 linha é melhor para não deformar a altura
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = if (containerColor == Color.White) contentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF1A1C1E),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container do Ícone (Fixo, não deforma)
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (containerColor == Color.White) contentColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (containerColor == colorResource(id = R.color.yellow_back)) {
                        contentColor
                    } else {
                        colorResource(id = R.color.yellow_back)
                    } )
            }

            // A MUDANÇA ESTÁ AQUI: weight(1f) e padding
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f) // Preenche o espaço restante sem deformar o card
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 14.sp // Ajuste leve para ser mais responsivo
                    ),
                    color = if (containerColor == Color.White) contentColor else Color.White,
                    maxLines = 1, // No menu, geralmente 1 linha é melhor para não deformar a altura
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = if (containerColor == Color.White) contentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}