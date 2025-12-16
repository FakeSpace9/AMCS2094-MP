package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.OrderRepository

class OrderHistoryViewModelFactory(
    private val repo: OrderRepository,
    private val authPrefs: AuthPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            return OrderHistoryViewModel(repo, authPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
