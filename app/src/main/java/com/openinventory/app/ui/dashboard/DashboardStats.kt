package com.openinventory.app.ui.dashboard

// Este objeto representa o estado da UI
data class ProductStats(
    val name: String,
    val quantity: Int,
    val revenue: Double
)

data class DashboardState(
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val salesCount: Int = 0,
    val topProducts: List<ProductStats> = emptyList()
)