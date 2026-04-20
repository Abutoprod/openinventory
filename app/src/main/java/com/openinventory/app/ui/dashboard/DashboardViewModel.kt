package com.openinventory.app.ui.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {
    private val db = Firebase.firestore

    // Usamos o DashboardState que definimos acima
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        observeDailyStats()
    }

    private fun observeDailyStats() {
        val dateId = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val statsRef = db.collection("daily_stats").document(dateId)

        // 1. Escuta o faturamento e lucro total do dia
        statsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                _uiState.update { it.copy(
                    totalRevenue = snapshot.getDouble("totalRevenue") ?: 0.0,
                    totalProfit = snapshot.getDouble("totalProfit") ?: 0.0,
                    salesCount = snapshot.getLong("count")?.toInt() ?: 0
                )}
            }
        }

        // 2. Escuta a subcoleção de produtos para o gráfico (Valor Agregado)
        statsRef.collection("top_products")
            .orderBy("totalRevenue", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, _ ->
                val products = snapshot?.map { doc ->
                    ProductStats(
                        name = doc.getString("name") ?: "Desconhecido",
                        quantity = doc.getLong("quantity")?.toInt() ?: 0,
                        revenue = doc.getDouble("totalRevenue") ?: 0.0
                    )
                } ?: emptyList()

                _uiState.update { it.copy(topProducts = products) }
            }
    }
}