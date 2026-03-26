package com.openinventory.app.data.datasource.local

// Mantenha assim para aceitar o nome do parâmetro como 'cliente' se preferir
data class ComandaFirebase(
    val id: String = "",
    val cliente: String = "", // Mudei de mesaOuCliente para cliente para bater com o repositório
    val status: String = "ABERTA",
    val total: Double = 0.0,
    val itens: List<ItemVenda> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
data class ItemVenda(
    val produtoId: String = "",
    val nome: String = "",
    val quantidade: Int = 0,
    val precoUnitario: Double = 0.0
)