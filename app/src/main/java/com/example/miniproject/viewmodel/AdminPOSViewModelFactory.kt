package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.repository.POSRepository
import com.example.miniproject.repository.PromotionRepository

class AdminPOSViewModelFactory(
    private val productDao: ProductDao,
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository // Add this
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminPOSViewModel(productDao, posRepository, promotionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}