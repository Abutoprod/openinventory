package com.openinventory.app.ui.sale
import java.util.Date
data class SaleModel(
    val customerName: String,
    val total: Double,
    val timestamp: Date,
    val items: List<SaleItem>,
    val cpf: String = ""
)

data class SaleItem(val name: String, val price: Double)