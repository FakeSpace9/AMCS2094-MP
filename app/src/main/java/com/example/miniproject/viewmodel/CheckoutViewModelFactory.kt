package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.PaymentRepository

class CheckoutViewModelFactory(
    private val cartRepository: CartRepository,
    private val addressRepository: AddressRepository,
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val authPreferences: AuthPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(
                cartRepository,
                addressRepository,
                paymentRepository,
                orderRepository,
                authPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}