package com.openinventory.app.ui.product
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.openinventory.app.data.database.entity.ProductEntity
@Composable
fun AddProductDialog(onDismiss: () -> Unit, onConfirm: (ProductEntity) -> Unit) {
    var code by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var custo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CONSUMIVEL") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("BOOSTER", "EVENTO", "CONSUMIVEL")
    var qty by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Item de Estoque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Seletor de Categoria (Tipo um ComboBox)
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoria: $category")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código/SKU") })
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição") })
                OutlinedTextField(
                    value = custo,
                    onValueChange = { custo = it },
                    label = { Text("Preço de Custo") })
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preço Venda") })
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Qtd Inicial") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newProd = ProductEntity(code, desc, 0.0, category,price.toDoubleOrNull() ?: 0.0, qty.toIntOrNull() ?: 0)
                onConfirm(newProd)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}