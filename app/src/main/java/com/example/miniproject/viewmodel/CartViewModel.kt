package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.PromotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository,
    private val promotionRepository: PromotionRepository
) : ViewModel() {
    val cartItems: StateFlow<List<CartEntity>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.Lazily,emptyList())

    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount
    val shippingFee = 10.0

    val total: StateFlow<Double> = combine(subtotal, _discountAmount) { sub, disc ->
        (sub + shippingFee - disc).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val promoCode = MutableStateFlow("")
    val promoCodeError = MutableStateFlow<String?>(null)

    fun updateQuantity(item: CartEntity, newQuantity: Int){
        viewModelScope.launch {
            repository.updateQuantity(item, newQuantity)
        }
    }

    fun removeFromCart(item: CartEntity){
        viewModelScope.launch {
            repository.removeCartItem(item)
        }
    }

    fun onPromoCodeChange(newCode:String){
        promoCode.value = newCode
        //clear error and type again
        if(promoCodeError.value != null){
            promoCodeError.value = null
        }
    }

    fun applyVoucherCode(){
        val code = promoCode.value.trim()
        if (code.isEmpty()) {
            promoCodeError.value = "Please enter a code"
            return
        }

        viewModelScope.launch {
            val promo = promotionRepository.getPromotionByCode(code)

            if (promo == null) {
                promoCodeError.value = "Invalid promo code"
                _discountAmount.value = 0.0
            } else {
                // Check Date Validity
                val now = System.currentTimeMillis()
                if (now < promo.startDate || now > promo.endDate) {
                    promoCodeError.value = "Promotion expired"
                    _discountAmount.value = 0.0
                } else {
                    // Success: Calculate Discount
                    promoCodeError.value = null // Clear error
                    val sub = subtotal.value

                    val discount = if (promo.isPercentage) {
                        sub * (promo.discountRate / 100)
                    } else {
                        promo.discountRate // Fixed amount
                    }
                    _discountAmount.value = discount
                }
            }
        }
    }
}

