package com.openinventory.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
// Arquivo: data/database/entity/ProductEntity.kt
@Entity(
    tableName = "product_bundle",
    primaryKeys = ["parentSku", "childSku"]
)
data class BundleEntity(
    val parentSku: String, // Código do Kit (ex: KIT01)
    val childSku: String,  // Código do item individual (ex: CARNE01)
    val quantity: Int      // Quantos itens desse código vão no kit
)