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

class OrderSuccessViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _order = MutableStateFlow<OrderEntity?>(null)
    val order: StateFlow<OrderEntity?> = _order

    private val _orderItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val orderItems: StateFlow<List<OrderItemEntity>> = _orderItems

    fun loadOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                _order.value = repository.getOrderById(orderId)
                _orderItems.value = repository.getOrderItems(orderId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class OrderSuccessViewModelFactory(
    private val repository: OrderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSuccessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderSuccessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}