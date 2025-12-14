package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: String,
    val orderDate: Long,
    val totalAmount: Double,
    val status: String,
    val shippingFee: Double,
    val grandTotal: Double,
    val discount: Double,
    val paymentMethod: String,
    val deliveryAddress: String
)

@Entity(tableName = "order_items")
data class OrderItemEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Long,
    val productId: String,
    val productName: String,
    val variantSku: String,
    val size: String,
    val color: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)