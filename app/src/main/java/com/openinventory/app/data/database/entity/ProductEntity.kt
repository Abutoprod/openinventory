package com.openinventory.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
// Arquivo: data/database/entity/ProductEntity.kt
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val code: String, // EAN/GTIN
    val description: String,
    val logicalStock: Int = 0,
    val price: Double = 0.0,
    val quantity: Int
)