package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carts")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    val productId: String,
    val variantSku : String,
    val productName:String,
    val productImageUrl:String,
    val selectedSize: String,
    val selectedColour: String,
    val price: Double,
    val quantity: Int
)



