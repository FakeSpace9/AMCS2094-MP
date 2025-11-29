package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val productId: String, // PK: Unique ID for the general product (e.g., "PROD-001")
    val productName: String,
    val productImg: String,
    val category: String,
    val gender: String,
    val price: Double,
    val description: String
)