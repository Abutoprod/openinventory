package com.openinventory.app.ui.comanda

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.ComandaResponseDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComandaScreen(viewModel: ComandaViewModel, currentStoreId: String) {
    val idFilial = currentStoreId.toLongOrNull() ?: 0L
    val context = LocalContext.current

    val comandas by viewModel.comandas.collectAsState()
    val statusAtivo by viewModel.filtroStatus.collectAsState()
    val comandaDetalhada by viewModel.comandaDetalhada.collectAsState()
    val produtosLoja by viewModel.produtosLoja.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showNovaComandaDialog by remember { mutableStateOf(false) }
    var showAddProdutoDialog by remember { mutableStateOf(false) }
    var comandaSelecionadaId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(idFilial) {
        viewModel.carregarComandas(idFilial)
        viewModel.carregarProdutosDaLoja(idFilial)
        viewModel.carregarUsuariosParaBusca()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA), // Fundo claro e moderno
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNovaComandaDialog = true },
                containerColor = Color(0xFFFFC107)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Comanda")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho Limpo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Comandas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                IconButton(onClick = { viewModel.carregarComandas(idFilial) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                }
            }

            // --- FILTROS (DO JEITO QUE VOCÊ FALOU QUE FUNCIONA) ---
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterCheckbox("Abertas", statusAtivo == "abertas") { viewModel.aplicarFiltroStatus("abertas", idFilial) }
                FilterCheckbox("Fechadas", statusAtivo == "fechadas") { viewModel.aplicarFiltroStatus("fechadas", idFilial) }
                FilterCheckbox("Todas", statusAtivo == "todas") { viewModel.aplicarFiltroStatus("todas", idFilial) }
            }

            // Pesquisa de Cliente (Lógica sua)
            Text("Filtrar por Cliente:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            CampoBuscaCliente(
                viewModel = viewModel,
                onClienteSelecionado = { cliente ->
                    viewModel.aplicarFiltroCliente(cliente, idFilial)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // ... dentro do LazyColumn
                    items(comandas) { comanda ->
                        CardComandaModerno(
                            comanda = comanda,
                            onAddClick = {
                                comandaSelecionadaId = comanda.id
                                showAddProdutoDialog = true
                            },
                            onFecharClick = {
                                viewModel.fecharComanda(comanda.id, idFilial)
                                enviarReciboWhatsApp(context, comanda)
                                Toast.makeText(context, "Comanda #${comanda.id} Finalizada!", Toast.LENGTH_SHORT).show()
                            },
                            onLongClick = { viewModel.carregarDetalhes(comanda.id) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Detalhes
    comandaDetalhada?.let { detalhes ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.limparDetalhes() },
            sheetState = sheetState
        ) {
            DetalhesComandaContent(detalhes)
        }
    }

    // Diálogos (Devem estar definidos no seu projeto)
    if (showNovaComandaDialog) {
        AbrirComandaDialog(viewModel, idFilial, { showNovaComandaDialog = false }) { user ->
            viewModel.abrirNovaComanda(user.id, idFilial)
            showNovaComandaDialog = false
        }
    }

    if (showAddProdutoDialog && comandaSelecionadaId != null) {
        AdicionarProdutoDialog(produtosLoja, { showAddProdutoDialog = false }) { prod, qtd ->
            viewModel.lancarProdutoNaComanda(comandaSelecionadaId!!, prod, qtd, idFilial)
            showAddProdutoDialog = false
        }
    }
}
@Composable
fun CardComandaModerno(
    comanda: ComandaResponseDTO,
    onAddClick: () -> Unit,
    onFecharClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // A SEGURANÇA REAL: Olha para o dado da comanda, não para o filtro da tela
    val estaAberta = comanda.aberta

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onLongClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            // Se fechada, fica cinza claro
            containerColor = if (estaAberta) Color.White else Color(0xFFF1F1F1)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = comanda.nomeCliente,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (estaAberta) Color.Black else Color.Gray
                )

                // Badge de Status para não ter erro
                Surface(
                    color = if (estaAberta) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (estaAberta) "ABERTA" else "FECHADA",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaAberta) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Text("ID: #${comanda.id}", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total", fontSize = 10.sp, color = Color.Gray)
                    Text("R$ ${"%.2f".format(comanda.valorTotal)}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.weight(1f))

                // TRAVA DE SEGURANÇA
                if (estaAberta) {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.AddShoppingCart, null, tint = Color(0xFF2E7D32))
                    }
                    Button(
                        onClick = onFecharClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("FINALIZAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Se estiver na aba "Todas" e a comanda for fechada, aparece o cadeado
                    Icon(Icons.Default.Lock, contentDescription = "Finalizada", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FilterCheckbox(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp),
        color = if (selected) Color(0xFFFFC107) else Color.Transparent,
        shape = RoundedCornerShape(20.dp) // Formato de pílula fica mais moderno
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.Black,
                    unselectedColor = Color.Gray
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.Black else Color.DarkGray,
                softWrap = false // EVITA QUEBRA DE TEXTO
            )
        }
    }
}
fun enviarReciboWhatsApp(context: Context, comanda: ComandaResponseDTO) {
    val texto = """
        *RECIBO DE PAGAMENTO* 📑
        ----------------------------
        Cliente: ${comanda.nomeCliente}
        Código: #${comanda.id}
        ----------------------------
        *VALOR TOTAL: R$ ${"%.2f".format(comanda.valorTotal)}*
        ----------------------------
        Obrigado pela preferência!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.startActivity(Intent.createChooser(intent, "Enviar Recibo"))
    }
}

@Composable
fun DetalhesComandaContent(detalhes: ComandaResponseDTO) {
    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
        Text("Itens da Comanda", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        detalhes.itens.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${item.quantidade}x ${item.produtoNome}")
                Text("R$ ${"%.2f".format(item.precoUnitario * item.quantidade)}")
            }
        }
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOTAL", fontWeight = FontWeight.Black)
            Text("R$ ${"%.2f".format(detalhes.valorTotal)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
        }
    }
}