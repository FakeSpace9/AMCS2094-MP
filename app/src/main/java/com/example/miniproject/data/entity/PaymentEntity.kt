package com.example.miniproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment")
data class PaymentEntity(
    @PrimaryKey
    val paymentId: Long = 0L ,
    val customerId: String = "",
    val paymentType: String = "",

    val displayName: String = "",
    val cardHolderName: String? = null,
    val cardNumber: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null,
    val cvv: String? = null,

    val walletId: String? = null,
    val isDefault: Boolean = false
)
