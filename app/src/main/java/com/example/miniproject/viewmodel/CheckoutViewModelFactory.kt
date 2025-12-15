package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.PaymentRepository
import com.example.miniproject.repository.ReceiptRepository

class CheckoutViewModelFactory(
    private val cartRepository: CartRepository,
    private val addressRepository: AddressRepository,
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val authPreferences: AuthPreferences,
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(
                cartRepository,
                addressRepository,
                paymentRepository,
                orderRepository,
                authPreferences,
                receiptRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}