package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.repository.POSRepository

class AdminPOSViewModelFactory(
    private val productDao: ProductDao,
    private val posRepository: POSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminPOSViewModel(productDao, posRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}