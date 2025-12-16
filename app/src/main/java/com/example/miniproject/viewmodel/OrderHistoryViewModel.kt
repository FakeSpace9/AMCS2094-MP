package com.example.miniproject.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderHistoryViewModel(
    private val repo: OrderRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    /* ---------- ORDER LIST ---------- */

    var orders by mutableStateOf<List<OrderEntity>>(emptyList())
        private set

    fun loadOrders() {
        val userId = authPrefs.getUserId() ?: return
        viewModelScope.launch {
            orders = repo.getOrdersByCustomer(userId)
        }
    }

    /* ---------- RECEIPT STATE ---------- */

    var selectedOrder by mutableStateOf<OrderEntity?>(null)
        private set

    var orderItems by mutableStateOf<List<OrderItemEntity>>(emptyList())
        private set

    /* ---------- LOAD ORDER RECEIPT ---------- */

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {

            // 1️⃣ Load order (contains total, address, payment method, etc.)
            val order = repo.getOrderById(orderId)
            selectedOrder = order

            // 2️⃣ Load order items
            orderItems = repo.getOrderItems(orderId.toLong())
        }
    }
}
