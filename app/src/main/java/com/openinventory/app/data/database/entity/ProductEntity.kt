package com.openinventory.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
// Arquivo: data/database/entity/ProductEntity.kt
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val code: String = "", // Valor padrão "" é essencial para o Firebase
    val description: String = "",
    val purchasePrice: Double = 0.0,
    val category: String = "CONSUMIVEL",
    val salePrice: Double = 0.0,
    val quantity: Int = 0,
    val storeId: String = ""
) {
    // Construtor vazio exigido pelo Firebase (se não usar valores padrão acima)
    constructor() : this("", "", 0.0, "", 0.0,0)
}