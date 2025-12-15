package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val cartItems: StateFlow<List<CartEntity>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.Lazily,emptyList())

    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val shippingFee = 10.0

    val total : StateFlow<Double> = subtotal.map{ sub->
        sub + shippingFee
    }.stateIn(viewModelScope, SharingStarted.Lazily,0.0)

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

    fun applyVoucherCode(){
        val code = promoCode.value
        if(code.equals("DISCOUNT10", ignoreCase = true)){
            promoCodeError.value = null
            //do the promo code here
        }else{
            promoCodeError.value = "Invalid promo code"
        }
    }

    fun onPromoCodeChange(newCode:String){
        promoCode.value = newCode
        //clear error and type again
        if(promoCodeError.value != null){
            promoCodeError.value = null
        }
    }
}

