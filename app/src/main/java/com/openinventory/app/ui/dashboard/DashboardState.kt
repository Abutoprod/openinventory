package com.openinventory.app.ui.dashboard


import com.github.mikephil.charting.data.PieEntry

data class DashboardState(
    val totalRecebido: Double = 0.0,
    val totalCusto: Double = 0.0,
    val lucro: Double = 0.0,
    val itensPizza: List<PieEntry> = emptyList(),
    val isLoading: Boolean = false // <-- ADICIONE ESTA LINHA
)