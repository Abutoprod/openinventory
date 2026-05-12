package com.openinventory.app.ui.comanda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.ProductResponse

@Composable
fun AdicionarProdutoDialog(
    produtosDisponiveis: List<ProductResponse>,
    onDismiss: () -> Unit,
    onConfirm: (ProductResponse, Int) -> Unit
) {
    var textoBusca by remember { mutableStateOf("") }
    var produtoSelecionado by remember { mutableStateOf<ProductResponse?>(null) }
    var quantidade by remember { mutableIntStateOf(1) }

    val produtosFiltrados = produtosDisponiveis.filter {
        it.description .contains(textoBusca, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (produtoSelecionado == null) "Selecionar Produto" else "Definir Quantidade") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (produtoSelecionado == null) {
                    // ETAPA 1: BUSCAR PRODUTO
                    OutlinedTextField(
                        value = textoBusca,
                        onValueChange = { textoBusca = it },
                        label = { Text("Nome do produto...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                        items(produtosFiltrados) { produto ->
                            ListItem(
                                headlineContent = { Text(produto.description) },
                                supportingContent = { Text("R$ ${"%.2f".format(produto.price)}") },
                                modifier = Modifier.clickable { produtoSelecionado = produto }
                            )
                        }
                    }
                } else {
                    // ETAPA 2: DEFINIR QUANTIDADE
                    Text(produtoSelecionado!!.description, style = MaterialTheme.typography.titleMedium)
                    Text("Preço unitário: R$ ${"%.2f".format(produtoSelecionado!!.price)}")

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (quantidade > 1) quantidade-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text(text = quantidade.toString(), fontSize = 24.sp, modifier = Modifier.padding(horizontal = 24.dp))
                        IconButton(onClick = { quantidade++ }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (produtoSelecionado != null) {
                Button(onClick = { onConfirm(produtoSelecionado!!, quantidade) }) {
                    Text("ADICIONAR")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (produtoSelecionado != null) produtoSelecionado = null // Volta para a busca
                else onDismiss()
            }) {
                Text(if (produtoSelecionado != null) "VOLTAR" else "CANCELAR")
            }
        }
    )
}