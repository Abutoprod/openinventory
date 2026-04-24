package com.openinventory.app.ui.product
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.openinventory.app.data.database.entity.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(onDismiss: () -> Unit, onConfirm: (ProductEntity) -> Unit) {
    var code by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var custo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CONSUMIVEL") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("BOOSTER", "EVENTO", "CONSUMIVEL", "PLAYMAT", "ACESSORE", "DECK")
    var qty by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabeçalho
                Column {
                    Text(
                        text = "Novo Produto",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preencha as informações do estoque",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Seletor de Categoria Estilizado
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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

                // Campo Código
                CustomProductTextField(
                    value = code,
                    onValueChange = { if (it.all { c -> c.isDigit() }) code = it },
                    label = "Código / SKU",
                    icon = Icons.Default.QrCode,
                    keyboardType = KeyboardType.Number
                )

                // Campo Descrição
                CustomProductTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "Descrição",
                    icon = Icons.Default.Description
                )

                // Grid para Preços (Dois campos lado a lado)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomProductTextField(
                        value = custo,
                        onValueChange = { input ->
                            val cleaned = input.replace(",", ".")
                            if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null || cleaned.endsWith(".")) custo = cleaned
                        },
                        label = "Custo",
                        icon = Icons.Default.AttachMoney,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        prefix = "R$"
                    )
                    CustomProductTextField(
                        value = price,
                        onValueChange = { input ->
                            val cleaned = input.replace(",", ".")
                            if (cleaned.isEmpty() || cleaned.toDoubleOrNull() != null || cleaned.endsWith(".")) price = cleaned
                        },
                        label = "Venda",
                        icon = Icons.Default.TrendingUp,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        prefix = "R$"
                    )
                }

                // Campo Quantidade
                CustomProductTextField(
                    value = qty,
                    onValueChange = { if (it.all { c -> c.isDigit() }) qty = it },
                    label = "Quantidade Inicial",
                    icon = Icons.Default.Inventory2,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newProd = ProductEntity(
                                code, desc, custo.toDoubleOrNull() ?: 0.0,
                                category, price.toDoubleOrNull() ?: 0.0, qty.toIntOrNull() ?: 0
                            )
                            onConfirm(newProd)
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Produto")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    prefix: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        prefix = prefix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
}