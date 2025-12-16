package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminOrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val statusOptions = listOf("New", "Processing", "Shipped", "Completed")

    // State for currently selected tab index
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    // The current status string based on the selected index
    private val currentStatus = MutableStateFlow(statusOptions[0])

    // Automatically fetches orders whenever currentStatus changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredOrders: StateFlow<List<OrderEntity>> = currentStatus
        .flatMapLatest { status ->
            orderRepository.getOrdersByStatusFlow(status)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        currentStatus.value = statusOptions[index]
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            // The UI will update automatically because Room Flow emits the change
            orderRepository.updateOrderStatus(orderId, newStatus)
        }
    }
}

class AdminOrderViewModelFactory(private val orderRepository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminOrderViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}