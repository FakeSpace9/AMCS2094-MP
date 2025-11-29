package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.Product
import com.example.miniproject.data.entity.ProductVariant
import com.example.miniproject.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    object Success : ProductState()
    data class Error(val message: String) : ProductState()
}

class AdminProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _productState = MutableStateFlow<ProductState>(ProductState.Idle)
    val productState: StateFlow<ProductState> = _productState

    fun saveProduct(
        name: String,
        category: String,
        gender: String,
        priceStr: String,
        stockStr: String,
        baseSku: String, // User types "SHIRT"
        color: String,
        sizes: Set<String>, // User selects "S", "M"
        description: String
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading

            // 1. Validation
            if (name.isBlank() || priceStr.isBlank() || baseSku.isBlank() || sizes.isEmpty()) {
                _productState.value = ProductState.Error("Please fill in all fields and select at least one size")
                return@launch
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            val stockPerItem = stockStr.toIntOrNull() ?: 0

            // 2. Create Parent Entity
            // IMPORTANT: We set productId = "" (Empty)
            // The Repository will automatically generate "PROD-001", "PROD-002", etc.
            val product = Product(
                productId = "",
                productName = name,
                productImg = "",
                category = category,
                gender = gender,
                price = price,
                description = description
            )

            // 3. Create Variant Entities
            val variantList = sizes.map { size ->
                // Logic:
                // If baseSku is "SHIRT" and size is "S", ID becomes "SHIRT-S"
                // If user only selects 1 size, we can just use "SHIRT" if you prefer.
                val uniqueVariantId = if (sizes.size > 1) "$baseSku-$size" else baseSku

                ProductVariant(
                    productVariantId = uniqueVariantId, // PK (The Barcode)
                    productId = "", // FK (Empty for now, Repository will fill it with PROD-XXX)
                    size = size,
                    color = color,
                    stock = stockPerItem
                )
            }

            // 4. Save
            val result = repository.saveProductWithVariants(product, variantList)

            if (result.isSuccess) {
                _productState.value = ProductState.Success
            } else {
                _productState.value = ProductState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _productState.value = ProductState.Idle
    }
}