package com.openinventory.app.data.database.entity
import androidx.room.Entity
@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "productCode"]
)
data class OrderItemEntity(
    val orderId: String,
    val productCode: String,
    val description: String,
    val quantity: Int,
    val unitPrice: Double
)