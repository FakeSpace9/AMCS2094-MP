package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val name: String,
    val email: String,
    val phone: String,
    val profilePictureUrl: String? = null
)