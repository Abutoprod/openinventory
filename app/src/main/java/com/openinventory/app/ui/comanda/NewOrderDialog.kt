package com.openinventory.app.ui.comanda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // ESSE IMPORT É O QUE RESOLVE O ERRO DA LISTA
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.Column

@Composable
fun NewOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    suggestions: List<String>
) {
    var customerName by remember { mutableStateOf("") }

    // Filtra os clientes conforme o que está sendo digitado
    val filteredSuggestions = suggestions.filter {
        it.contains(customerName, ignoreCase = true) && customerName.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Comanda") },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Lista de sugestões rápidas
                if (filteredSuggestions.isNotEmpty()) {
                    Text("Sugestões:", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(filteredSuggestions) { name ->
                            TextButton(
                                onClick = { customerName = name },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(customerName) },
                enabled = customerName.isNotBlank()
            ) {
                Text("Abrir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}