    package com.example.miniproject.viewmodel

import java.util.UUID

data class VariantUiState(
    val id: String = UUID.randomUUID().toString(),
    var size: String = "",
    var colour: String = "",
    var sku: String = "",
    var price: String = "",
    var quantity: String = ""
)

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    object Success : ProductState()
    data class Error(val message: String) : ProductState()
}

enum class SortOption {
    NEWEST,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    NAME_A_Z
}