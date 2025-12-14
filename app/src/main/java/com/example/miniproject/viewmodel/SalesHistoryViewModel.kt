package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.repository.POSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SalesHistoryViewModel(
    private val posRepository: POSRepository

) : ViewModel() {

    // --- Stats State ---
    private val _todaySales = MutableStateFlow(0.0)
    val todaySales: StateFlow<Double> = _todaySales

    private val _todayItems = MutableStateFlow(0)

    private val _todayOrders = MutableStateFlow(0)
    val todayOrders: StateFlow<Int> = _todayOrders

    fun syncOrders() {
        viewModelScope.launch {
            // 1. Sync Cloud to Local
            posRepository.syncPOSOrders()

            // 2. Refresh Stats from Local DB
            loadDashboardStats()
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _todaySales.value = posRepository.getTodaySales()
            _todayOrders.value = posRepository.getTodayOrderCount()
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