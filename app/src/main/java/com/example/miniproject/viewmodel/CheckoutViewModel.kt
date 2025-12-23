package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.data.entity.PromotionEntity
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.PaymentRepository
import com.example.miniproject.repository.PromotionRepository
import com.example.miniproject.repository.ReceiptItem
import com.example.miniproject.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepo: CartRepository,
    private val addressRepo: AddressRepository,
    private val paymentRepo: PaymentRepository,
    private val orderRepo: OrderRepository,
    private val promotionRepo: PromotionRepository,
    private val authPrefs: AuthPreferences,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _filterIds = MutableStateFlow<Set<Int>?>(null)

    val cartItems: StateFlow<List<CartEntity>> = combine(cartRepo.allCartItems, _filterIds) { allItems, filter ->
        if (filter == null) allItems else allItems.filter { it.id in filter }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _userAddresses = MutableStateFlow<List<AddressEntity>>(emptyList())
    val userAddresses: StateFlow<List<AddressEntity>> = _userAddresses

    private val _userPayments = MutableStateFlow<List<PaymentEntity>>(emptyList())
    val userPayments: StateFlow<List<PaymentEntity>> = _userPayments

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress

    private val _selectedPayment = MutableStateFlow<PaymentEntity?>(null)
    val selectedPayment: StateFlow<PaymentEntity?> = _selectedPayment

    val promoCode = MutableStateFlow("")
    val promoCodeError = MutableStateFlow<String?>(null)

    private val _activePromotion = MutableStateFlow<PromotionEntity?>(null)

    val shippingFee = 10.0

    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val discountAmount: StateFlow<Double> = combine(subtotal, _activePromotion) { sub, promo ->
        if (promo == null) {
            0.0
        } else {
            if (promo.isPercentage) {
                sub * (promo.discountRate / 100)
            } else {
                promo.discountRate
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val grandTotal: StateFlow<Double> = combine(subtotal, discountAmount) { sub, disc ->
        (sub + shippingFee - disc).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _orderState = MutableStateFlow<Result<Long>?>(null)
    val orderState: StateFlow<Result<Long>?> = _orderState

    init {
        refreshData()
    }

    fun setSelectedItemIds(idsString: String?) {
        if (!idsString.isNullOrEmpty()) {
            val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            _filterIds.value = ids
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val userId = authPrefs.getUserId() ?: return@launch
            val addresses = addressRepo.getAddressesByCustomerId(userId)
            _userAddresses.value = addresses
            val payments = paymentRepo.getPayments(userId)
            _userPayments.value = payments
        }
    }

    fun selectAddressById(id: Long) {
        val addr = _userAddresses.value.find { it.addressId == id }
        if (addr != null) _selectedAddress.value = addr
    }

    fun selectPaymentById(id: Long) {
        val pay = _userPayments.value.find { it.paymentId == id }
        if (pay != null) _selectedPayment.value = pay
    }


    fun onPromoCodeChange(newCode:String){
        promoCode.value = newCode
        if (newCode.isBlank()) {
            _activePromotion.value = null
            promoCodeError.value = null
        } else {
            // Only clear error if typing, but keep active promo until invalid
            if(promoCodeError.value != null){
                promoCodeError.value = null
            }
        }
    }

    fun clearPromo() {
        promoCode.value = ""
        _activePromotion.value = null
        promoCodeError.value = null
    }

    fun applyPromoCode() {
        val code = promoCode.value.trim()
        if (code.isEmpty()) return

        viewModelScope.launch {
            val promo = promotionRepo.getPromotionByCode(code)
            if (promo == null) {
                promoCodeError.value = "Invalid promo code"
                _activePromotion.value = null
            } else {
                val now = System.currentTimeMillis()
                if (now < promo.startDate || now > promo.endDate) {
                    promoCodeError.value = "Promotion expired"
                    _activePromotion.value = null
                } else {
                    promoCodeError.value = null
                    _activePromotion.value = promo
                }
            }
        }
    }

    fun payNow() {
        val userId = authPrefs.getUserId()
        val address = _selectedAddress.value
        val payment = _selectedPayment.value
        val items = cartItems.value
        val total = grandTotal.value
        val currentSubtotal = subtotal.value
        val currentDiscount = discountAmount.value

        if (userId == null || address == null || payment == null || items.isEmpty()) {
            _orderState.value = Result.failure(Exception("Missing details"))
            return
        }

        viewModelScope.launch {
            val addressSnapshot = "${address.fullName}, ${address.phone}, ${address.addressLine1}, ${address.postcode}"
            val paymentSnapshot = "${payment.paymentType} - ${payment.displayName}"

            val order = OrderEntity(
                customerId = userId,
                customerEmail = authPrefs.getLoggedInEmail() ?: "",
                orderDate = System.currentTimeMillis(),
                totalAmount = currentSubtotal,
                shippingFee = shippingFee,
                discount = currentDiscount,
                grandTotal = total,
                status = "New",
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

            val cartItemIds = items.map { it.id }
            val result = orderRepo.placeOrder(order, orderItems, cartItemIds)
            _orderState.value = result

            if (result.isSuccess) {
                promoCode.value = ""
                promoCodeError.value = null
                _activePromotion.value = null

                val email = authPrefs.getLoggedInEmail() ?: "customer@example.com"
                val receiptItems = items.map {
                    ReceiptItem(
                        name = it.productName,
                        variant = "${it.selectedSize} / ${it.selectedColour}",
                        quantity = it.quantity,
                        unitPrice = it.price,
                        totalPrice = it.price * it.quantity,
                        imageUrl = it.productImageUrl
                    )
                }
                receiptRepository.triggerEmail(
                    toEmail = email,
                    orderId = result.getOrNull().toString(),
                    customerName = address.fullName,
                    items = receiptItems,
                    subTotal = currentSubtotal,
                    deliveryFee = shippingFee,
                    discountAmount = currentDiscount,
                )
            }
        }
    }

    fun resetOrderState() {
        _orderState.value = null
    }
}