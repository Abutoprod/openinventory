package com.openinventory.app.ui.ponto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openinventory.app.service.RankingDTO
import com.openinventory.app.service.UsuarioResponse
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(viewModel: PontosViewModel, filialId: Long) {
    val calendar = Calendar.getInstance()
    var mesSelecionado by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var anoSelecionado by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    var showLancamentoSheet by remember { mutableStateOf(false) }
    var expandedMes by remember { mutableStateOf(false) }
    var expandedAno by remember { mutableStateOf(false) }

    var clienteSelecionado by remember { mutableStateOf<UsuarioResponse?>(null) }
    var buscaCliente by remember { mutableStateOf("") }
    var pontosInput by remember { mutableStateOf("") }

    // Definição de Cores do Tema Claro
    val backgroundColor = Color(0xFFF5F7FA) // Cinza bem clarinho (fundo)
    val surfaceColor = Color.White // Cards e Inputs
    val primaryText = Color(0xFF1A1A1A) // Quase preto
    val secondaryText = Color(0xFF757575) // Cinza médio
    val accentColor = Color(0xFFFFC107) // Amarelo Ouro/Amber para destaque

    LaunchedEffect(Unit) { viewModel.inicializar(filialId) }

    LaunchedEffect(Unit) { viewModel.inicializar(filialId) }

    // CORREÇÃO AQUI: Adicionamos mesSelecionado e anoSelecionado como chaves do LaunchedEffect
    // Assim, sempre que um deles mudar, a função de atualizar o ranking é disparada.
    LaunchedEffect(viewModel.jogoSelecionado, mesSelecionado, anoSelecionado) {
        if (viewModel.jogoSelecionado != null) {
            // Certifique-se de que sua função atualizarRanking no ViewModel aceite ou use
            // as variáveis de mês e ano selecionados.
            viewModel.atualizarRanking(filialId, mesSelecionado, anoSelecionado)
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { buscaCliente = ""; showLancamentoSheet = true },
                containerColor = accentColor,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Lançar", modifier = Modifier) },
                text = { Text("LANÇAR PONTOS", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("RANKING", color = primaryText, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("Desempenho mensal da filial", color = secondaryText, fontSize = 14.sp)
                }
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataChipClaro(label = "Mês $mesSelecionado", expanded = expandedMes, onExpand = { expandedMes = true }, modifier = Modifier.weight(1f))
                DataChipClaro(label = anoSelecionado.toString(), expanded = expandedAno, onExpand = { expandedAno = true }, modifier = Modifier.weight(1f))
            }

            Box {
                DropdownMenu(expanded = expandedMes, onDismissRequest = { expandedMes = false }) {
                    (1..12).forEach { m ->
                        DropdownMenuItem(text = { Text("Mês $m") }, onClick = { mesSelecionado = m; expandedMes = false })
                    }
                }
                DropdownMenu(expanded = expandedAno, onDismissRequest = { expandedAno = false }) {
                    val atual = calendar.get(Calendar.YEAR)
                    (atual downTo atual - 1).forEach { a ->
                        DropdownMenuItem(text = { Text(a.toString()) }, onClick = { anoSelecionado = a; expandedAno = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (viewModel.jogos.isNotEmpty()) {
                val selectedIdx = viewModel.jogos.indexOf(viewModel.jogoSelecionado).coerceAtLeast(0)
                ScrollableTabRow(
                    selectedTabIndex = selectedIdx,
                    containerColor = Color.Transparent,
                    divider = {},
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIdx]),
                            color = accentColor
                        )
                    }
                ) {
                    viewModel.jogos.forEach { jogo ->
                        Tab(
                            selected = viewModel.jogoSelecionado == jogo,
                            onClick = { viewModel.selecionarJogo(jogo, filialId) },
                            text = {
                                Text(
                                    text = jogo.nome,
                                    fontWeight = if (viewModel.jogoSelecionado == jogo) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            selectedContentColor = primaryText,
                            unselectedContentColor = secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentColor) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    itemsIndexed(viewModel.ranking) { index, player ->
                        RankingCardClaro(posicao = index + 1, player = player)
                    }
                }
            }
        }
    }

    if (showLancamentoSheet) {
        ModalBottomSheet(onDismissRequest = { showLancamentoSheet = false }, containerColor = surfaceColor) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Lançar Pontuação", color = primaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = buscaCliente,
                    onValueChange = { buscaCliente = it },
                    placeholder = { Text("Pesquisar player...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier, tint = secondaryText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                val filtrados = viewModel.clientes.filter { it.nome.contains(buscaCliente, true) }
                LazyRow(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtrados) { cliente ->
                        val selecionado = clienteSelecionado == cliente
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selecionado) accentColor else Color.LightGray.copy(alpha = 0.3f))
                                .clickable { clienteSelecionado = cliente }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(cliente.nome, color = if (selecionado) Color.Black else primaryText, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                OutlinedTextField(
                    value = pontosInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) pontosInput = it },
                    label = { Text("Quantidade de Pontos") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.lancarPontos(clienteSelecionado?.id ?: 0, pontosInput.toIntOrNull() ?: 0, "Lançamento App", filialId, viewModel.jogoSelecionado?.id ?: 0) {
                            viewModel.atualizarRanking(filialId)
                            showLancamentoSheet = false
                            pontosInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = clienteSelecionado != null && pontosInput.isNotEmpty() && !viewModel.isLancando
                ) {
                    if (viewModel.isLancando) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    else Text("CONFIRMAR PONTOS", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun DataChipClaro(label: String, expanded: Boolean, onExpand: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onExpand() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier,
                tint = Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun RankingCardClaro(posicao: Int, player: RankingDTO) {
    val corDestaque = when (posicao) {
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFF9E9E9E)
        3 -> Color(0xFFCD7F32)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)) // Sombra suave para o card branco
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = if(posicao <= 3) 2.dp else 0.dp,
                color = corDestaque.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (posicao == 1) {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = "Campeão",
                tint = corDestaque,
                modifier = Modifier.size(34.dp).padding(end = 4.dp)
            )
        } else {
            Text(
                text = posicao.toString(),
                color = if (posicao <= 3) corDestaque else Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF0F0F0), CircleShape)
                .border(1.dp, if(posicao <= 3) corDestaque else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.nome.take(1).uppercase(),
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = player.nome,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = player.pontos.toString(),
                color = Color(0xFF1A1A1A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "PONTOS",
                color = Color(0xFF9B870C), // Tom mais escuro de dourado para ler no branco
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}