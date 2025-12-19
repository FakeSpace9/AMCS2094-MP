package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Parent Product (General Info)
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val description: String,
    val category: String,
    val gender: String,
    val imageUrl: String = ""
)

@Entity(
    tableName = "product_images",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Int = 0,
    val productId: String,
    val imageUrl: String
)
@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true) val variantId: Int = 0,
    val productId: String,
    val size: String,
    val colour: String,
    val sku: String,
    val price: Double,
    val stockQuantity: Int
)