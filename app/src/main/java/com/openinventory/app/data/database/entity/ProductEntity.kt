package com.openinventory.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val code: String = "",
    val description: String = "",
    val purchasePrice: Double = 0.0,
    val category: String = "CONSUMIVEL",
    val salePrice: Double = 0.0,
    val quantity: Int = 0,
    val storeId: String = "" // O Firebase vai usar este valor padrão e preencher com o do banco
)
// APAGUEI O CONSTRUTOR EXTRA QUE ESTAVA QUEBRANDO TUDO