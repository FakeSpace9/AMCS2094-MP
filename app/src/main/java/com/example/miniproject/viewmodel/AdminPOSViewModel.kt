package com.example.miniproject.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.POSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Simple data class for Admin POS items (in-memory)
data class POSItem(
    val variantId: Int,
    val productId: String,
    val sku: String,
    val name: String,
    val size: String,
    val color: String,
    val price: Double,
    val imageUrl: String,
    var quantity: Int
)

class AdminPOSViewModel(
    private val productDao: ProductDao,
    private val posRepository: POSRepository
) : ViewModel() {

    // Transaction State
    private val _posItems = mutableStateListOf<POSItem>()
    val posItems: List<POSItem> get() = _posItems

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _checkoutState = MutableStateFlow<Result<Long>?>(null)
    val checkoutState: StateFlow<Result<Long>?> = _checkoutState

    // Form Data
    var customerEmail = MutableStateFlow("")
    var promoCode = MutableStateFlow("")
    var selectedPaymentMethod = MutableStateFlow("")

    fun onScanSku(sku: String) {
        viewModelScope.launch {
            val cleanSku = sku.trim().uppercase()

            // 1. Check if already in list
            val existing = _posItems.find { it.sku == cleanSku }
            if (existing != null) {
                existing.quantity++
                calculateTotal()
                return@launch
            }

            // 2. Lookup in DB
            val variant = productDao.getVariantBySku(cleanSku)
            if (variant == null) {
                _message.value = "SKU not found: $sku"
                return@launch
            }

            val product = productDao.getProductById(variant.productId)
            if (product == null) return@launch

            // 3. Add to list
            _posItems.add(
                POSItem(
                    variantId = variant.variantId,
                    productId = product.productId,
                    sku = variant.sku,
                    name = product.name,
                    size = variant.size,
                    color = variant.colour,
                    price = variant.price,
                    imageUrl = product.imageUrl,
                    quantity = 1
                )
            )
            calculateTotal()
        }
    }

    fun updateQuantity(item: POSItem, newQty: Int) {
        if (newQty <= 0) {
            _posItems.remove(item)
        } else {
            val index = _posItems.indexOf(item)
            if (index != -1) {
                _posItems[index] = item.copy(quantity = newQty)
            }
        }
        calculateTotal()
    }

    private fun calculateTotal() {
        _totalAmount.value = _posItems.sumOf { it.price * it.quantity }
    }

    fun completeOrder(adminId: String) {
        if (_posItems.isEmpty()) return
        if (selectedPaymentMethod.value.isEmpty()) {
            _message.value = "Please select a payment method"
            return
        }

        viewModelScope.launch {
            val total = _totalAmount.value
            val discount = if(promoCode.value.isNotEmpty()) 0.0 else 0.0 // Logic later
            val finalTotal = total - discount

            // --- USE NEW POS ENTITY ---
            val order = POSOrderEntity(
                cashierId = adminId,
                customerEmail = customerEmail.value.ifBlank { null },
                orderDate = System.currentTimeMillis(),
                totalAmount = total,
                discount = discount,
                grandTotal = finalTotal,
                paymentMethod = selectedPaymentMethod.value,
                status = "Completed"
            )

            val orderItems = _posItems.map {
                POSOrderItemEntity(
                    posOrderId = 0, // Will be set by Room auto-gen
                    productId = it.productId,
                    productName = it.name,
                    variantSku = it.sku,
                    size = it.size,
                    color = it.color,
                    price = it.price,
                    quantity = it.quantity
                )
            }

            // --- CALL POS REPOSITORY ---
            _checkoutState.value = posRepository.placePOSOrder(order, orderItems)
        }
    }

    fun clearMessage() { _message.value = null }

    fun resetOrder() {
        _posItems.clear()
        _totalAmount.value = 0.0
        customerEmail.value = ""
        promoCode.value = ""
        selectedPaymentMethod.value = ""
        _checkoutState.value = null
    }
}

