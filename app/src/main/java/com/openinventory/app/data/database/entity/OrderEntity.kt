package com.openinventory.app.data.database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String = UUID.randomUUID().toString(),
    val customerName: String = "",
    val isOpen: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0
)