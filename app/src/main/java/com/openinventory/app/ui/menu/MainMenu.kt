package com.openinventory.app.ui.menu


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(
    onNavigateToScan: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToHistory: () -> Unit
) {

    val ImpactFontFamily = FontFamily(
        Font(R.font.nautiluspompilius, FontWeight.Normal)
    )
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6A1B9A)),
                //.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ico),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Open Inventory",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ImpactFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção de Boas-vindas ou Status
            Text(
                text = "O que deseja fazer hoje?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Start)
            )

            // Botão Principal: Iniciar Coleta
            MenuActionCard(
                title = "Nova Coleta",
                description = "Iniciar contagem de inventário",
                icon = Icons.Default.CameraAlt,
                containerColor = Color(0xFFE1F5FE),
                iconColor = Color(0xFF0288D1),
                onClick = onNavigateToScan
            )

            // Linha com opções secundárias
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecondaryMenuCard(
                    modifier = Modifier.weight(1f),
                    title = "Importar",
                    subtitle = "Catálogo Excel",
                    icon = Icons.Default.ShoppingCart,
                    onClick = onNavigateToImport
                )
                SecondaryMenuCard(
                    modifier = Modifier.weight(1f),
                    title = "Histórico",
                    subtitle = "Ver Seções",
                    icon = Icons.Default.List,
                    onClick = onNavigateToHistory
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Rodapé com versão ou info da empresa
            Text(
                text = "v1.0.0 - DAUX Sistemas", // Referência à sua empresa atual
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}