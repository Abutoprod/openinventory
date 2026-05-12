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
fun AbrirComandaDialog(
    viewModel: ComandaViewModel,
    filialId: Long,
    onDismiss: () -> Unit,
    onConfirm: (UsuarioResponse) -> Unit // Adicione esta linha se não existir
) {
    var textoBusca by remember { mutableStateOf("") }
    val usuariosSugeridos by viewModel.sugestoesClientes.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Comanda") },
        text = {
            Column {
                OutlinedTextField(
                    value = textoBusca,
                    onValueChange = {
                        textoBusca = it
                        viewModel.filtrarClientesParaBusca(it)
                    },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (usuariosSugeridos.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(usuariosSugeridos) { usuario ->
                            ListItem(
                                headlineContent = { Text(usuario.nome) },
                                supportingContent = { Text(usuario.email) },
                                modifier = Modifier.clickable {
                                    onConfirm(usuario)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}