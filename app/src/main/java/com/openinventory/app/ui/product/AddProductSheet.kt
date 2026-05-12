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
import com.openinventory.app.service.ProductResponsecreate

// ... imports permanecem os mesmos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    onDismiss: () -> Unit,
    onConfirm: (ProductDTO) -> Unit,
    currentStoreId: String,
    productToEdit: ProductResponse? = null // Mude para ProductResponse
) {
    val isEditing = productToEdit != null

    // Agora usamos os campos que adicionamos no modelo
    var campoCodigo by remember { mutableStateOf(productToEdit?.code ?: "") }
    var campoDescricao by remember { mutableStateOf(productToEdit?.description ?: "") }
    var campoPrecoC by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "") } // Agora funciona!
    var campoPrecoV by remember { mutableStateOf(productToEdit?.price?.toString() ?: "") }
    var campoQtd by remember { mutableStateOf(productToEdit?.quantity?.toString() ?: "") }
    var categoriaSelecionada by remember { mutableStateOf(productToEdit?.category ?: "BOOSTER") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(
                text = if (isEditing) "Alterar Produto" else "Novo Produto",
                fontSize = 20.sp, fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = campoCodigo,
                onValueChange = { campoCodigo = it },
                label = { Text("Código") },
                enabled = !isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = campoDescricao,
                onValueChange = { campoDescricao = it },
                label = { Text("Descrição") },
                enabled = !isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = categoriaSelecionada,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                enabled = !isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = campoPrecoC, onValueChange = { campoPrecoC = it }, label = { Text("Compra") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = campoPrecoV, onValueChange = { campoPrecoV = it }, label = { Text("Venda") }, modifier = Modifier.weight(1f))
            }

            OutlinedTextField(value = campoQtd, onValueChange = { campoQtd = it }, label = { Text("Quantidade") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    // Mapeamento EXPLICITO para o DTO
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (isEditing) "SALVAR ALTERAÇÕES" else "CADASTRAR")
            }
        }
    }
}