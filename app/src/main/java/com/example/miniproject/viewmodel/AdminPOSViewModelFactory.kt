package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.repository.POSRepository
import com.example.miniproject.repository.PromotionRepository
import com.example.miniproject.repository.ReceiptRepository

class AdminPOSViewModelFactory(
    private val productDao: ProductDao,
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminPOSViewModel(productDao, posRepository, promotionRepository,receiptRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}