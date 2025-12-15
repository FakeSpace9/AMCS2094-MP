package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.PromotionRepository

class CartViewModelFactory (private val repository: CartRepository, private val promotionRepository: PromotionRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository,promotionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }

}