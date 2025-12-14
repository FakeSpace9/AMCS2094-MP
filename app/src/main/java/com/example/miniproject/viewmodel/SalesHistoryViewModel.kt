package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.repository.POSRepository
import kotlinx.coroutines.launch

class SalesHistoryViewModel(
    private val posRepository: POSRepository
) : ViewModel() {

    fun syncOrders() {
        viewModelScope.launch {
            // This pulls from Firestore and saves to Room
            posRepository.syncPOSOrders()
        }
    }
}

class SalesHistoryViewModelFactory(
    private val posRepository: POSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesHistoryViewModel(posRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}