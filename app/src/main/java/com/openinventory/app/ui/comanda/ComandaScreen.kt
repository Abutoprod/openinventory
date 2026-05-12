package com.openinventory.app.ui.comanda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.ComandaResponseDTO
import com.openinventory.app.service.UsuarioResponse
import androidx.compose.foundation.lazy.LazyRow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComandaScreen(viewModel: ComandaViewModel, currentStoreId: String) {
    val idFilial = currentStoreId.toLongOrNull() ?: 0L
    val comandas by viewModel.comandas.collectAsState()
    val statusAtivo by viewModel.filtroStatus.collectAsState()
    val comandaDetalhada by viewModel.comandaDetalhada.collectAsState()
    val produtosLoja by viewModel.produtosLoja.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState()

    // Estados para diálogos
    var showNovaComandaDialog by remember { mutableStateOf(false) }
    var showAddProdutoDialog by remember { mutableStateOf(false) }
    var comandaSelecionadaId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(idFilial) {
        viewModel.carregarComandas(idFilial)
        viewModel.carregarProdutosDaLoja(idFilial)
        viewModel.carregarUsuariosParaBusca()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showNovaComandaDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Comanda")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Cabeçalho e Filtros
            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Comandas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.carregarComandas(idFilial) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterCheckbox("Abertas", statusAtivo == "abertas") { viewModel.aplicarFiltroStatus("abertas", idFilial) }
                        FilterCheckbox("Fechadas", statusAtivo == "fechadas") { viewModel.aplicarFiltroStatus("fechadas", idFilial) }
                        FilterCheckbox("Todas", statusAtivo == "todas") { viewModel.aplicarFiltroStatus("todas", idFilial) }
                    }
                    // --- ADICIONE ISSO AQUI: Filtro por Cliente ---
                    val clienteSelecionado by viewModel.clienteSelecionado.collectAsState()

                    Text("Pesquisar Cliente para Filtro:", style = MaterialTheme.typography.labelSmall)
                    CampoBuscaCliente(
                        viewModel = viewModel,
                        onClienteSelecionado = { cliente ->
                            viewModel.aplicarFiltroCliente(cliente, idFilial)
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(comandas) { comanda ->
                        CardComanda(
                            comanda = comanda,
                            onAddClick = {
                                comandaSelecionadaId = comanda.id
                                showAddProdutoDialog = true
                            },
                            onFecharClick = { viewModel.fecharComanda(comanda.id, idFilial)},
                            onLongClick = {
                                viewModel.carregarDetalhes(comanda.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // --- CORREÇÃO AQUI: Uso do .let para evitar NullPointerException no fechamento ---
    comandaDetalhada?.let { detalhes ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.limparDetalhes() },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    text = "Itens: ${detalhes.nomeCliente}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (detalhes.itens.isEmpty()) {
                    Text("Nenhum item lançado.", color = Color.Gray, modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(detalhes.itens) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.produtoNome, fontWeight = FontWeight.Medium)
                                    Text("${item.quantidade}x R$ ${"%.2f".format(item.precoUnitario)}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("R$ ${"%.2f".format(item.precoUnitario * item.quantidade)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("R$ ${"%.2f".format(detalhes.valorTotal)}",
                        color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showNovaComandaDialog) {
        AbrirComandaDialog(
            viewModel = viewModel,
            filialId = idFilial,
            onDismiss = { showNovaComandaDialog = false },
            onConfirm = { usuario ->
                viewModel.abrirNovaComanda(usuario.id, idFilial)
                showNovaComandaDialog = false
            }
        )
    }

    if (showAddProdutoDialog && comandaSelecionadaId != null) {
        AdicionarProdutoDialog(
            produtosDisponiveis = produtosLoja,
            onDismiss = { showAddProdutoDialog = false },
            onConfirm = { produto, qtd ->
                viewModel.lancarProdutoNaComanda(comandaSelecionadaId!!, produto, qtd, idFilial)
                showAddProdutoDialog = false
            }
        )
    }
}
@Composable
fun FilterCheckbox(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }.padding(4.dp)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
fun FiltroClienteBar(
    clientes: List<UsuarioResponse>,
    clienteAtual: UsuarioResponse?,
    onClienteSelecionado: (UsuarioResponse?) -> Unit
) {
    // Aqui você pode usar um ExposedDropdownMenu ou uma linha de Chips
    LazyRow(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        item {
            FilterChip(
                selected = clienteAtual == null,
                onClick = { onClienteSelecionado(null) },
                label = { Text("Todos Clientes") }
            )
        }
        items(clientes) { cliente ->
            FilterChip(
                selected = clienteAtual?.id == cliente.id,
                onClick = { onClienteSelecionado(cliente) },
                label = { Text(cliente.nome) }
            )
        }
    }
}