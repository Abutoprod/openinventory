package com.openinventory.app.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.ProductDTO
import com.openinventory.app.service.ProductResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    onDismiss: () -> Unit,
    onConfirm: (ProductDTO) -> Unit,
    currentStoreId: String,
    productToEdit: ProductResponse? = null
) {
    val isEditing = productToEdit != null
    val listaCategorias = listOf("BOOSTER", "BOOSTER BOX", "DECK", "ACESSORIO", "SINGLE", "CONSUMIVEL", "OUTROS")

    // ESTADOS DOS CAMPOS
    var campoCodigo by remember { mutableStateOf(productToEdit?.code ?: "") }
    var campoDescricao by remember { mutableStateOf(productToEdit?.description ?: "") }
    var campoPrecoC by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "") }
    var campoPrecoV by remember { mutableStateOf(productToEdit?.price?.toString() ?: "") }
    var campoQtd by remember { mutableStateOf(productToEdit?.quantity?.toString() ?: "") }
    var categoriaSelecionada by remember { mutableStateOf(productToEdit?.category ?: "BOOSTER") }

    // ESTADO PARA CONTROLAR O MENU DROPDOWN
    var expandedCategoria by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp) // Espaço extra para o teclado não cobrir o botão
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) "Editar Produto" else "Novo Produto",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = campoCodigo,
                onValueChange = { campoCodigo = it },
                label = { Text("Código/EAN") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isEditing // Geralmente não mudamos o código na edição
            )

            OutlinedTextField(
                value = campoDescricao,
                onValueChange = { campoDescricao = it },
                label = { Text("Descrição do Produto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- CAMPO DE CATEGORIA COM DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = expandedCategoria,
                onExpandedChange = { if (!isEditing) expandedCategoria = !expandedCategoria }
            ) {
                OutlinedTextField(
                    value = categoriaSelecionada,
                    onValueChange = {},
                    readOnly = true, // Impede digitação manual
                    label = { Text("Categoria") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isEditing,
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategoria,
                    onDismissRequest = { expandedCategoria = false }
                ) {
                    listaCategorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                categoriaSelecionada = categoria
                                expandedCategoria = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            // ---------------------------------------

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = campoPrecoC,
                    onValueChange = { campoPrecoC = it },
                    label = { Text("Preço Compra") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = campoPrecoV,
                    onValueChange = { campoPrecoV = it },
                    label = { Text("Preço Venda") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = campoQtd,
                onValueChange = { campoQtd = it },
                label = { Text("Quantidade em Stock") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val novoDto = ProductDTO(
                        codigo = campoCodigo,
                        descricao = campoDescricao,
                        precoCompra = campoPrecoC.toDoubleOrNull() ?: 0.0,
                        precoVenda = campoPrecoV.toDoubleOrNull() ?: 0.0,
                        quantidade = campoQtd.toIntOrNull() ?: 0,
                        categoria = categoriaSelecionada,
                        filialId = currentStoreId.toLong()
                    )
                    onConfirm(novoDto)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isEditing) "SALVAR ALTERAÇÕES" else "CADASTRAR PRODUTO")
            }
        }
    }
}