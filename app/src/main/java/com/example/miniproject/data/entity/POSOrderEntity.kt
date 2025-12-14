package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_orders")
data class POSOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cashierId: String,       // Who processed the order (Admin ID)
    val customerEmail: String?,  // Optional for Walk-in
    val orderDate: Long,
    val totalAmount: Double,
    val discount: Double,
    val grandTotal: Double,
    val paymentMethod: String,
    val status: String = "Completed" // POS orders are usually instant
)

@Entity(tableName = "pos_order_items")
data class POSOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val posOrderId: Long,        // Links to POSOrderEntity
    val productId: String,
    val productName: String,
    val variantSku: String,
    val size: String,
    val color: String,
    val price: Double,
    val quantity: Int
)