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
import com.openinventory.app.ui.import.ImportViewModel
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
import androidx.compose.ui.res.colorResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.openinventory.app.ui.import.ImportSheet
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(
    onNavigateToScan: () -> Unit,
    importViewModel: ImportViewModel,
    onNavigateToHistory: () -> Unit
) {
    var showImportSheet by remember { mutableStateOf(false) }

    // 1. Esse estado vai ser controlado dentro do seu ImportViewModel futuramente
    // ou por um callback que vamos passar para o ImportSheet
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val ImpactFontFamily = FontFamily(
        Font(R.font.nautiluspompilius, FontWeight.Normal)
    )

    // O Box permite que o Loading fique "por cima" do Scaffold
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.basic_purple)),
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
                // Botão Principal: Nova Coleta
                MenuActionCard(
                    title = "Nova Coleta",
                    description = "Iniciar contagem de inventário",
                    icon = Icons.Default.CameraAlt,
                    containerColor = colorResource(R.color.basic_lilas),
                    iconColor =  colorResource(R.color.dark_purple),
                    onClick = onNavigateToScan
                )

                // Linha com Importar e Histórico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryMenuCard(
                        modifier = Modifier.weight(1f),
                        title = "Importar",
                        subtitle = "Catálogo Excel",
                        icon = Icons.Default.ShoppingCart,
                        onClick  = { showImportSheet = true }
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

                Text(
                    text = "v1.0.0 - DAUX Sistemas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }

        // 2. CAMADA DE LOADING (Flutua sobre o Scaffold)
        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.5f) // Fundo semi-transparente
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.basic_purple))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processando CSV...", color = Color.White)
                }
            }
        }
    } // FIM DO BOX

    // 3. BOTTOM SHEET
    if (showImportSheet) {
        ImportSheet(
            viewModel = importViewModel,
            onDismiss = { showImportSheet = false }
            // DICA: Se você adicionar um callback 'onLoading' no seu ImportSheet,
            // você consegue mudar o valor de 'isLoading' aqui!
        )
    }
}