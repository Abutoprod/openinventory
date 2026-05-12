package com.openinventory.app.ui.comanda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openinventory.app.service.UsuarioResponse

@Composable
fun CampoBuscaCliente(
    viewModel: ComandaViewModel,
    onClienteSelecionado: (UsuarioResponse) -> Unit
) {
    var textoDigitado by remember { mutableStateOf("") }

    // CORRIGIDO: Agora aponta para 'sugestoesClientes' que existe no seu ViewModel
    val sugestoes by viewModel.sugestoesClientes.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedTextField(
            value = textoDigitado,
            onValueChange = {
                textoDigitado = it
                // CORRIGIDO: Nome da função correta no ViewModel
                viewModel.filtrarClientesParaBusca(it)
            },
            label = { Text("Buscar Cliente (Nome)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Se houver sugestões, mostra a lista
        if (sugestoes.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                LazyColumn {
                    items(sugestoes) { cliente ->
                        ListItem(
                            headlineContent = { Text(cliente.nome) },
                            supportingContent = { Text(cliente.email) },
                            modifier = Modifier.clickable {
                                textoDigitado = cliente.nome
                                // Limpa a busca após selecionar
                                viewModel.filtrarClientesParaBusca("")
                                onClienteSelecionado(cliente)
                            }
                        )
                    }
                }
            }
        }
    }
}