package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["productId"], // The PK in Parent
            childColumns = ["productId"],  // The FK in this table
            onDelete = ForeignKey.CASCADE  // If Product is deleted, delete its variants
        )
    ],
    // Indexing the FK is good practice for speed
    indices = [Index(value = ["productId"])]
)
data class ProductVariant(
    @PrimaryKey val productVariantId: String, // PK: This is the SKU (e.g., "PROD-001-S")
    val productId: String,                    // FK: Links to Product table
    val size: String,
    val color: String,
    val stock: Int
)