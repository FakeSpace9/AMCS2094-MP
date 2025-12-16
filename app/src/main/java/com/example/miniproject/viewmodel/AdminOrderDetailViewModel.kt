package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminOrderDetailViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _order = MutableStateFlow<OrderEntity?>(null)
    val order: StateFlow<OrderEntity?> = _order

    private val _orderItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val orderItems: StateFlow<List<OrderItemEntity>> = _orderItems

    fun loadOrderDetails(orderId: Long) {
        viewModelScope.launch {
            _order.value = orderRepository.getOrderById(orderId)
            _orderItems.value = orderRepository.getOrderItems(orderId)
        }
    }
}

class AdminOrderDetailViewModelFactory(
    private val orderRepository: OrderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminOrderDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminOrderDetailViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}