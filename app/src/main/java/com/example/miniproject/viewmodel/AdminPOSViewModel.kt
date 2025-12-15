package com.example.miniproject.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.example.miniproject.repository.POSRepository
import com.example.miniproject.repository.PromotionRepository
import com.example.miniproject.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

// Updated Data Class with maxStock
data class POSItem(
    val variantId: Int,
    val productId: String,
    val sku: String,
    val name: String,
    val size: String,
    val color: String,
    val price: Double,
    val imageUrl: String,
    var quantity: Int,
    val maxStock: Int // Added to track available stock
)

class AdminPOSViewModel(
    private val productDao: ProductDao,
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _subTotal = MutableStateFlow(0.0)
    val subTotal: StateFlow<Double> = _subTotal

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount

    private val _posItems = mutableStateListOf<POSItem>()
    val posItems: List<POSItem> get() = _posItems

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _checkoutState = MutableStateFlow<Result<Long>?>(null)
    val checkoutState: StateFlow<Result<Long>?> = _checkoutState

    var customerEmail = MutableStateFlow("")
    var promoCode = MutableStateFlow("")
    var selectedPaymentMethod = MutableStateFlow("")
    val currentDate = Date()

    fun onScanSku(sku: String) {
        viewModelScope.launch {
            val cleanSku = sku.trim().uppercase()

            // 1. Fetch Variant to check real-time stock
            val variant = productDao.getVariantBySku(cleanSku)
            if (variant == null) {
                _message.value = "SKU not found: $sku"
                return@launch
            }

            // 2. Check if item is already in the cart
            val existing = _posItems.find { it.sku == cleanSku }

            if (existing != null) {
                // Check if adding 1 more exceeds available stock
                if (existing.quantity + 1 <= variant.stockQuantity) {
                    existing.quantity++
                    calculateTotal()
                } else {
                    _message.value = "Insufficient stock! Max available: ${variant.stockQuantity}"
                }
                return@launch
            }

            // 3. New Item - Check if any stock exists
            if (variant.stockQuantity <= 0) {
                _message.value = "Item is Out of Stock"
                return@launch
            }

            val product = productDao.getProductById(variant.productId)
            if (product == null) return@launch

            // 4. Add to list with maxStock
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
                    quantity = 1,
                    maxStock = variant.stockQuantity // Store the limit
                )
            )
            calculateTotal()
        }
    }

    fun updateQuantity(item: POSItem, newQty: Int) {
        if (newQty <= 0) {
            _posItems.remove(item)
        } else {
            // Check against maxStock before increasing
            if (newQty > item.maxStock) {
                _message.value = "Cannot add more. Only ${item.maxStock} left in stock."
                return
            }

            val index = _posItems.indexOf(item)
            if (index != -1) {
                _posItems[index] = item.copy(quantity = newQty)
            }
        }
        calculateTotal()
    }

    private fun calculateTotal() {
        val sub = _posItems.sumOf { it.price * it.quantity }
        _subTotal.value = sub

        // Recalculate discount if a code is already applied
        if (promoCode.value.isNotEmpty()) {
            _totalAmount.value = sub - _discountAmount.value
        } else {
            _totalAmount.value = sub
        }
    }

    fun applyPromoCode() {
        val code = promoCode.value.trim().uppercase()
        if (code.isEmpty()) return

        viewModelScope.launch {
            val promo = promotionRepository.getPromotionByCode(code)

            if (promo == null) {
                _message.value = "Invalid Promo Code"
                _discountAmount.value = 0.0
            } else {
                // Check Date Validity
                val now = System.currentTimeMillis()
                if (now < promo.startDate || now > promo.endDate) {
                    _message.value = "Promotion Expired"
                    _discountAmount.value = 0.0
                } else {
                    // Calculate Discount
                    val sub = _subTotal.value
                    val discount = if (promo.isPercentage) {
                        sub * (promo.discountRate / 100)
                    } else {
                        promo.discountRate // Fixed amount
                    }

                    _discountAmount.value = discount
                    _totalAmount.value = (sub - discount).coerceAtLeast(0.0)
                    _message.value = "Code Applied: ${promo.name}"
                }
            }
        }
    }

    fun completeOrder(adminId: String) {
        if (_posItems.isEmpty()) return
        if (selectedPaymentMethod.value.isEmpty()) {
            _message.value = "Please select a payment method"
            return
        }

        viewModelScope.launch {
            val order = POSOrderEntity(
                cashierId = adminId,
                customerEmail = customerEmail.value.ifBlank { null },
                orderDate = currentDate,
                totalAmount = _subTotal.value,     // Store original subtotal
                discount = _discountAmount.value,  // Store discount
                grandTotal = _totalAmount.value,   // Store final total
                paymentMethod = selectedPaymentMethod.value,
                status = "Completed"
            )

            // ... existing items mapping ...
            val orderItems = _posItems.map {
                POSOrderItemEntity(
                    posOrderId = 0,
                    productId = it.productId,
                    productName = it.name,
                    variantSku = it.sku,
                    size = it.size,
                    color = it.color,
                    price = it.price,
                    quantity = it.quantity
                )
            }

            val result = posRepository.placePOSOrder(order, orderItems)
            _checkoutState.value = result


            if (result.isSuccess) {
                val email = customerEmail.value
                if (email.isNotBlank()) {
                    // Generate a simple HTML list for the email
                    val itemsHtml = _posItems.joinToString("") { item ->
                        "<tr><td>${item.name} (${item.size})</td><td align='right'>RM ${item.price}</td></tr>"
                    }

                    receiptRepository.triggerEmail(
                        toEmail = email,
                        customerName = "Valued Customer",
                        itemsDescription = itemsHtml,
                        totalAmount = _totalAmount.value
                    )

                }
            }
        }
    }

    fun clearMessage() { _message.value = null }

    fun resetOrder() {
        _posItems.clear()
        _subTotal.value = 0.0
        _discountAmount.value = 0.0
        _totalAmount.value = 0.0
        customerEmail.value = ""
        promoCode.value = ""
        selectedPaymentMethod.value = ""
        _checkoutState.value = null
    }
}