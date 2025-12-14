package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val promotionId: String, // UUID for Firestore sync
    val adminId: String,
    val code: String,        // e.g., "SALE20"
    val name: String,        // e.g., "Year End Sale"
    val description: String,
    val discountRate: Double,// e.g., 0.10 for 10% or 10.0 for RM10 (You define logic)
    val startDate: Long,
    val endDate: Long,
    val isPercentage: Boolean = true // Flag to distinguish % vs Fixed RM
)