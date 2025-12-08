package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.repository.CartRepository

class ProductDetailScreenViewModelFactory(
    private val productDao : ProductDao,
    private val cartRepository: CartRepository
): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailScreenViewModel::class.java)) {
            @Suppress("UNCHECK_CAST")
            return ProductDetailScreenViewModel(productDao,cartRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}