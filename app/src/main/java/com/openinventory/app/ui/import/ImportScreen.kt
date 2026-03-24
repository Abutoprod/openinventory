package com.openinventory.app.ui.import
// Dentro da ImportScreen.kt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.ui.res.colorResource
import com.openinventory.app.R
// ESTES DOIS IMPORTS ABAIXO SÃO O QUE RESOLVEM O ERRO DO 'by'
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.* // Para Column, Spacer, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    viewModel: ImportViewModel,
    onDismiss: () -> Unit // Função para fechar o "pop-up"
) {
    val sheetState = rememberModalBottomSheetState()
    val state by viewModel.uiState.collectAsState()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    // O componente que cria a "telinha por cima"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Importar Dados", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(24.dp))

            // Seletor de Arquivo
            OutlinedButton(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedUri == null) "Escolher Arquivo CSV" else "Arquivo Selecionado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback de Status
            when (state) {
                is ImportUiState.Loading -> CircularProgressIndicator()
                is ImportUiState.Success -> Text("Concluído!", color = Color(0xFF388E3C))
                is ImportUiState.Error -> Text("Erro no arquivo", color = Color.Red)
                else -> Text("O arquivo deve conter colunas: Nome, SKU, Qtd.")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Ação
            Button(
                onClick = { selectedUri?.let { viewModel.importFile(it) } },
                enabled = selectedUri != null && state !is ImportUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Importação")
            }
        }
    }
}