package com.openinventory.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import androidx.compose.foundation.clickable
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, filialId: Long) {
    val state by viewModel.uiState.collectAsState()

    // LaunchedEffect para carregar os dados iniciais
    LaunchedEffect(Unit) {
        viewModel.carregarDados(filialId)
    }

    // O Surface resolve o problema do fundo acompanhar o modo do celular (Dark/Light)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(
                "Dashboard de Vendas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Chama o componente novo
            FiltroPeriodo(onFiltrar = { inicio, fim ->
                viewModel.carregarDados(filialId, inicio, fim)
            })

            Spacer(Modifier.height(16.dp))

            // Linha com Cards de Valores
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResumoCard("Vendas", state.totalRecebido, Color(0xFF4CAF50), Modifier.weight(1f))
                ResumoCard("Custo", state.totalCusto, Color(0xFFF44336), Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            ResumoCard("Lucro Estimado", state.lucro, Color(0xFF2196F3), Modifier.fillMaxWidth())

            Spacer(Modifier.height(32.dp))
            Text(
                "Produtos Mais Vendidos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Gráfico de Pizza
            if (state.itensPizza.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                            legend.textColor = android.graphics.Color.GRAY // Ajuste para ler no dark
                            setHoleColor(android.graphics.Color.TRANSPARENT)
                            setEntryLabelColor(android.graphics.Color.BLACK)
                            animateY(1000)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    update = { chart ->
                        val dataSet = PieDataSet(state.itensPizza, "").apply {
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                            valueTextSize = 12f
                        }
                        chart.data = PieData(dataSet)
                        chart.invalidate()
                    }
                )
            } else if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
            } else {
                Text("Nenhuma venda no período", modifier = Modifier.padding(top = 20.dp))
            }
        }
    }
}


@Composable
fun FiltroPeriodo(onFiltrar: (String, String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Datas para exibição (Brasil: DD/MM/AAAA)
    var dataVisualInicio by remember { mutableStateOf("") }
    var dataVisualFim by remember { mutableStateOf("") }

    // Datas para lógica/API (ISO: AAAA-MM-DD)
    var dataIsoInicio by remember { mutableStateOf("") }
    var dataIsoFim by remember { mutableStateOf("") }

    // Função para abrir o calendário
    fun abrirCalendario(onDataSelecionada: (String, String) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dia = dayOfMonth.toString().padStart(2, '0')
                val mes = (month + 1).toString().padStart(2, '0')
                val visual = "$dia/$mes/$year"
                val iso = "$year-$mes-$dia"
                onDataSelecionada(visual, iso)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filtrar Período", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Campo Início
                OutlinedTextField(
                    value = dataVisualInicio,
                    onValueChange = {},
                    label = { Text("Início") },
                    modifier = Modifier.weight(1f).clickable {
                        abrirCalendario { vis, iso -> dataVisualInicio = vis; dataIsoInicio = iso }
                    },
                    enabled = false, // Desabilita digitação, força o clique
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Campo Fim
                OutlinedTextField(
                    value = dataVisualFim,
                    onValueChange = {},
                    label = { Text("Fim") },
                    modifier = Modifier.weight(1f).clickable {
                        abrirCalendario { vis, iso -> dataVisualFim = vis; dataIsoFim = iso }
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Button(
                onClick = { onFiltrar(dataIsoInicio, dataIsoFim) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = dataIsoInicio.isNotEmpty() && dataIsoFim.isNotEmpty()
            ) {
                Text("Aplicar Filtro")
            }
        }
    }
}

@Composable
fun ResumoCard(titulo: String, valor: Double, cor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cor.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = cor)
            // USANDO A FUNÇÃO DE MOEDA PARA FORMATAR R$ 1.000,00
            Text(
                text = formatarMoeda(valor),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun formatarMoeda(valor: Double): String {
    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatador.format(valor)
}