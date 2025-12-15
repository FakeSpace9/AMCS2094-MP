package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.PaymentRepository
import com.example.miniproject.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepo: CartRepository,
    private val addressRepo: AddressRepository,
    private val paymentRepo: PaymentRepository,
    private val orderRepo: OrderRepository,
    private val authPrefs: AuthPreferences,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    // --- Data Streams ---
    val cartItems: StateFlow<List<CartEntity>> = cartRepo.allCartItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _userAddresses = MutableStateFlow<List<AddressEntity>>(emptyList())
    val userAddresses: StateFlow<List<AddressEntity>> = _userAddresses

    private val _userPayments = MutableStateFlow<List<PaymentEntity>>(emptyList())
    val userPayments: StateFlow<List<PaymentEntity>> = _userPayments

    // --- Selection State ---
    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress

    private val _selectedPayment = MutableStateFlow<PaymentEntity?>(null)
    val selectedPayment: StateFlow<PaymentEntity?> = _selectedPayment

    // --- Totals Calculation ---
    val shippingFee = 10.0
    val discount = 0.0

    // FIXED: Use .map instead of combine for single flow transformation
    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // FIXED: Use .map instead of combine
    val grandTotal: StateFlow<Double> = subtotal.map { sub ->
        sub + shippingFee - discount
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _orderState = MutableStateFlow<Result<Long>?>(null)
    val orderState: StateFlow<Result<Long>?> = _orderState

    init {
        refreshData()
    }

    // Call this when entering the screen to ensure latest data (Auto-fill logic)
    fun refreshData() {
        viewModelScope.launch {
            val userId = authPrefs.getUserId() ?: return@launch

            // 1. Load Addresses
            val addresses = addressRepo.getAddressesByCustomerId(userId)
            _userAddresses.value = addresses

            // Auto-select logic: Keep current selection if valid, else pick Default, else First
            if (_selectedAddress.value == null || addresses.none { it.addressId == _selectedAddress.value?.addressId }) {
                _selectedAddress.value = addresses.find { it.isDefault } ?: addresses.firstOrNull()
            }

            // 2. Load Payments
            val payments = paymentRepo.getPayments(userId)
            _userPayments.value = payments

            // Auto-select logic
            if (_selectedPayment.value == null || payments.none { it.paymentId == _selectedPayment.value?.paymentId }) {
                _selectedPayment.value = payments.find { it.isDefault } ?: payments.firstOrNull()
            }
        }
    }

    // Called when returning from AddressScreen with a selection
    fun selectAddressById(id: Long) {
        val addr = _userAddresses.value.find { it.addressId == id }
        if (addr != null) _selectedAddress.value = addr
    }

    // Called when returning from PaymentMethodScreen with a selection
    fun selectPaymentById(id: Long) {
        val pay = _userPayments.value.find { it.paymentId == id }
        if (pay != null) _selectedPayment.value = pay
    }

    fun payNow() {
        val userId = authPrefs.getUserId()
        val address = _selectedAddress.value
        val payment = _selectedPayment.value
        val items = cartItems.value
        val total = grandTotal.value

        if (userId == null || address == null || payment == null || items.isEmpty()) {
            _orderState.value = Result.failure(Exception("Missing details"))
            return
        }

        viewModelScope.launch {
            val addressSnapshot = "${address.fullName}, ${address.phone}, ${address.addressLine1}, ${address.postcode}"
            val paymentSnapshot = "${payment.paymentType} - ${payment.displayName}"

            val order = OrderEntity(
                customerId = userId,
                orderDate = System.currentTimeMillis(),
                totalAmount = subtotal.value,
                shippingFee = shippingFee,
                discount = discount,
                grandTotal = total,
                status = "Paid",
                deliveryAddress = addressSnapshot,
                paymentMethod = paymentSnapshot
            )

            val orderItems = items.map {
                OrderItemEntity(
                    orderId = 0,
                    productId = it.productId,
                    productName = it.productName,
                    variantSku = it.variantSku,
                    size = it.selectedSize,
                    color = it.selectedColour,
                    price = it.price,
                    quantity = it.quantity,
                    imageUrl = it.productImageUrl
                )
            }

            val result = orderRepo.placeOrder(order, orderItems)
            _orderState.value = result

            // 2. Trigger Email Receipt (Fixed Logic)
            if (result.isSuccess) {
                val email = authPrefs.getLoggedInEmail() ?: "customer@example.com" // Use AuthPrefs

                // Convert CartEntity items to HTML
                val itemsHtml = items.joinToString("") { item ->
                    """
                    <tr>
                        <td style="padding: 8px; border-bottom: 1px solid #ddd;">
                            ${item.productName} (${item.selectedSize})<br>
                            <span style="color: #888; font-size: 12px;">Qty: ${item.quantity}</span>
                        </td>
                        <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: right;">
                            RM ${String.format("%.2f", item.price * item.quantity)}
                        </td>
                    </tr>
                    """.trimIndent()
                }

                // Call the Repository
                receiptRepository.triggerEmail(
                    toEmail = email,
                    customerName = address.fullName, // Use name from address
                    itemsDescription = itemsHtml,
                    totalAmount = total
                )

            }
        }
    }

    fun resetOrderState() {
        _orderState.value = null
    }
}