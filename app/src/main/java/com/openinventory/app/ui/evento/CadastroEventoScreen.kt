package com.openinventory.app.ui.eventos

import androidx.compose.ui.graphics.Brush
import com.openinventory.app.R
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Event
import android.util.Log
import androidx.compose.ui.res.colorResource
import com.openinventory.app.service.JogoResponseDTO
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import androidx.compose.foundation.shape.CircleShape
import com.openinventory.app.service.EventoDTO
import java.util.Calendar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.ui.BiasAlignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CadastroEventoScreen(viewModel: EventoViewModel, filialId: Long, onBack: () -> Unit) {
    // ESTADOS DO FORMULÁRIO (Mantidos conforme solicitado)
    var isSemanal by remember { mutableStateOf(true) }
    var tituloManual by remember { mutableStateOf("") }
    var imagemManual by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var dataIso by remember { mutableStateOf("") }
    var dataVisual by remember { mutableStateOf("Selecionar Data e Hora") }
    var alinhamentoFoto by remember { mutableFloatStateOf(0f) }
    var jogoSelecionado by remember { mutableStateOf<JogoResponseDTO?>(null) }
    var expandedJogos by remember { mutableStateOf(false) }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }

    // Controla se a janelinha está aberta
    var showParticipantesSheet by remember { mutableStateOf(false) }
    var eventoSelecionado by remember { mutableStateOf<EventoDTO?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imagemUri = uri
    }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val tituloFinal = if (isSemanal && jogoSelecionado != null) "Semanal ${jogoSelecionado!!.nome}" else tituloManual
    val imagemFinal = if (isSemanal && jogoSelecionado != null) "semanal_${jogoSelecionado!!.nome.lowercase()}.jpg" else imagemManual
    var eventoParaExcluir by remember { mutableStateOf<EventoDTO?>(null) }
    LaunchedEffect(Unit) {
        viewModel.carregarJogos()
        viewModel.carregarEventos()
    }
    if (eventoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { eventoParaExcluir = null },
            title = { Text("Excluir Evento") },
            text = { Text("Tem certeza que deseja apagar o evento ${eventoParaExcluir?.titulo}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.excluirEvento(eventoParaExcluir!!.id)
                    eventoParaExcluir = null
                }) {
                    Text("EXCLUIR", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventoParaExcluir = null }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    // Estilo padrão para os campos de texto "Glass"
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.White.copy(alpha = 0.1f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
        cursorColor = Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.orange_back).copy(alpha = 0.9f),
                        colorResource(R.color.yellow_back).copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Gerenciar Eventos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // CARD DO FORMULÁRIO (GLASS EFFECT)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Switch Semanal
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Evento Semanal Padrão?", color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            Switch(
                                checked = isSemanal,
                                onCheckedChange = { isSemanal = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Campo Título
                        OutlinedTextField(
                            value = tituloFinal,
                            onValueChange = { if (!isSemanal) tituloManual = it },
                            label = { Text("Título do Evento") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSemanal,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )

                        Spacer(Modifier.height(12.dp))

                        // Dropdown de Jogos
                        ExposedDropdownMenuBox(
                            expanded = expandedJogos,
                            onExpandedChange = { expandedJogos = !expandedJogos }
                        ) {
                            OutlinedTextField(
                                value = jogoSelecionado?.nome ?: "Selecione o Jogo",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jogo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJogos) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = expandedJogos,
                                onDismissRequest = { expandedJogos = false }
                            ) {
                                viewModel.jogos.forEach { jogo ->
                                    DropdownMenuItem(
                                        text = { Text(jogo.nome) },
                                        onClick = {
                                            jogoSelecionado = jogo
                                            expandedJogos = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Seção da Imagem
                        Text(
                            "Imagem do Evento",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { launcher.launch("image/*") }
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imagemUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imagemUri).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    alignment = BiasAlignment(0f, alinhamentoFoto),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
                                    Text("Adicionar Foto", color = Color.White)
                                }
                            }
                        }

                        if (imagemUri != null) {
                            Slider(
                                value = alinhamentoFoto,
                                onValueChange = { alinhamentoFoto = it },
                                valueRange = -1f..1f,
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Botão Data
                        Button(
                            onClick = {
                                DatePickerDialog(context, { _, y, m, d ->
                                    TimePickerDialog(context, { _, hh, mm ->
                                        dataVisual = "%02d/%02d/%04d %02d:%02d".format(d, m + 1, y, hh, mm)
                                        dataIso = "%04d-%02d-%02dT%02d:%02d:00".format(y, m + 1, d, hh, mm)
                                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(dataVisual, color = Color.White)
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            label = { Text("Descrição do Evento") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            minLines = 2
                        )

                        Spacer(Modifier.height(24.dp))

                        // Botão Cadastrar (Destaque Branco)
                        Button(
                            onClick = {
                                jogoSelecionado?.let { jogo ->
                                    viewModel.cadastrarEventoComImagem(
                                        context, imagemUri, tituloFinal, descricao, dataIso,
                                        filialId, jogo.id, jogo.nome, isSemanal, imagemFinal
                                    ) {
                                        viewModel.carregarEventos()
                                        onBack()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = colorResource(R.color.orange_back)
                            ),
                            enabled = tituloFinal.isNotEmpty() && dataIso.isNotEmpty() && jogoSelecionado != null && !viewModel.isLoading.value
                        ) {
                            if (viewModel.isLoading.value) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorResource(R.color.orange_back))
                            } else {
                                Text("CADASTRAR EVENTO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Eventos no Servidor",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            items(viewModel.listaEventos) { evento ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Usamos combinedClickable para ter clique simples e longo
                        .combinedClickable(
                            onClick = {
                                eventoSelecionado = evento
                                viewModel.carregarParticipantes(evento.id)
                                showParticipantesSheet = true
                            },
                            onLongClick = {
                                // Quando segurar, preparamos o evento para ser excluído
                                // Isso vai disparar o AlertDialog que você já tem no topo da tela
                                eventoParaExcluir = evento
                            }
                        )
                ) {
                    EventoCard(evento)
                }
            }
        }
        if (showParticipantesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showParticipantesSheet = false },
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = eventoSelecionado?.titulo ?: "Participantes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        // viewModel.participantes agora é List<String>
                        text = "${viewModel.participantes.size} pessoas inscritas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Yellow.copy(alpha = 0.8f) // Destaque para a contagem
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.carregandoParticipantes.value) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Yellow)
                        }
                    } else if (viewModel.participantes.isEmpty()) {
                        Text(
                            "Nenhuma inscrição confirmada ainda.",
                            modifier = Modifier.padding(vertical = 20.dp),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // MUDANÇA AQUI: 'participante' agora é o próprio nome (String)
                            items(viewModel.participantes) { nome ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar usando a primeira letra da String 'nome'
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.Yellow, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = nome.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        // Exibe a String 'nome' diretamente
                                        Text(
                                            text = nome,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Inscrito via pré-cadastro",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

    @Composable
    fun EventoCard(evento: EventoDTO) {
        // 1. Lógica para corrigir a URL da Imagem (Troca localhost pela BASE_URL do Retrofit)
        val baseUrlCorrigida = "https://perennial-camisole-unaired.ngrok-free.dev"
        var urlTratada = evento.urlImagem.replace("http://localhost:8080", baseUrlCorrigida)

        if (urlTratada.contains("/uploads/")) {
            val partes = urlTratada.split("/uploads/")
            val nomeArquivoLimpo = partes[1].replace(" ", "_").replace(":", "")
            urlTratada = "${partes[0]}/uploads/$nomeArquivoLimpo"
        }

        val imagemUrlFinal = urlTratada
        Log.e("CARD", "URL Corrigida: $imagemUrlFinal")
        // 2. Formatação da Data
        val dataFormatada = try {
            val dataHora = LocalDateTime.parse(evento.dataHora)
            dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
        } catch (e: Exception) {
            evento.dataHora // fallback caso falte o parse
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp) // Aumentei um pouco a elevação
        ) {
            Box(modifier = Modifier.height(260.dp)) { // Aumentei levemente a altura para acomodar a descrição com folga

                // 1. Imagem de Fundo
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagemUrlFinal)
                        .addHeader("ngrok-skip-browser-warning", "true")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 2. GRADIENTE MELHORADO (O segredo do UX)
                // Criamos um gradiente que vai do transparente ao preto sólido
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                // Adicionamos uma cor intermediária para o degradê ser mais suave (Smooth)
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f), // Meio do card começa a escurecer
                                    Color.Black.copy(alpha = 0.9f)  // Base bem escura para leitura perfeita
                                ),
                                // Fazemos o gradiente ocupar os últimos 70% da imagem
                                startY = 150f
                            )
                        )
                )

                // 3. Conteúdo do Texto
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    // Badge da Filial
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = evento.filialNome.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Título com sombra leve (Shadow) para destacar da imagem
                    Text(
                        text = evento.titulo,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                blurRadius = 8f
                            )
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Descrição com fundo semi-transparente ou sombra
                    if (!evento.descricao.isNullOrBlank()) {
                        Text(
                            text = evento.descricao,
                            style = MaterialTheme.typography.bodySmall.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    blurRadius = 4f
                                )
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Event, // Opcional: Ícone de calendário
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = evento.jogoNome,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        Text("  •  ", color = Color.Gray)
                        Text(
                            text = dataFormatada,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }