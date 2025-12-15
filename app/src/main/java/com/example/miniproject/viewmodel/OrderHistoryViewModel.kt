package com.example.miniproject.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.repository.OrderRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class OrderHistoryViewModel(
    private val repo: OrderRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    var orders by mutableStateOf<List<OrderEntity>>(emptyList())
        private set

    fun loadOrders() {
        val userId = authPrefs.getUserId() ?: return

        viewModelScope.launch {
            orders = repo.getOrdersByCustomer(userId)
        }
    }

    suspend fun getOrderDetails(orderId: Long): List<OrderItemEntity> {
        return repo.getOrderItems(orderId)
    }
}

