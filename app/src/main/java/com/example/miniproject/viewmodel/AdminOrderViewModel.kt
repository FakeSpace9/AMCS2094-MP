package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.ReceiptItem
import com.example.miniproject.repository.ReceiptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminOrderViewModel(
    private val orderRepository: OrderRepository,
    private val receiptRepository: ReceiptRepository
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

    fun updateStatus(orderId: Int, newStatus: String) { // orderId is Int
        viewModelScope.launch {
            // 1. Update Database & Firestore
            val result = orderRepository.updateOrderStatus(orderId, newStatus)

            // 2. If successful, send email
            if (result.isSuccess) {
                // We need to fetch the order to get the customer's email
                val order = orderRepository.getOrderById(orderId.toLong())

                val orderItems = orderRepository.getOrderItems(orderId.toLong())

                if (order.customerEmail.isNotEmpty()) {

                    // Map Entity -> ReceiptItem
                    val receiptItems = orderItems.map {
                        ReceiptItem(
                            name = it.productName,
                            variant = "${it.size} / ${it.color}",
                            quantity = it.quantity,
                            unitPrice = it.price,
                            totalPrice = it.price * it.quantity,
                            imageUrl = it.imageUrl
                        )
                    }

                    // 4. Send Email with Items
                    receiptRepository.sendStatusUpdateEmail(
                        toEmail = order.customerEmail,
                        orderId = orderId.toString(),
                        newStatus = newStatus,
                        items = receiptItems // <--- Pass items here
                    )
                }
            }
        }
    }
}

class AdminOrderViewModelFactory(
    private val orderRepository: OrderRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminOrderViewModel(orderRepository, receiptRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}