package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true)
    val addressId: Long = 0L,
    val customerId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val addressLine1: String = "",
    val postcode: String = "",
    val label: String = "Home",
    val isDefault: Boolean = false
)
