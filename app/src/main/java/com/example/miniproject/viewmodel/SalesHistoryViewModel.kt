package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.example.miniproject.repository.POSRepository
import com.example.miniproject.repository.PromotionRepository
import com.example.miniproject.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.util.Date

class SalesHistoryViewModel(
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    // --- Stats State ---
    private val _todaySales = MutableStateFlow(0.0)
    val todaySales: StateFlow<Double> = _todaySales

    private val _todayItems = MutableStateFlow(0)

    private var originalItemsList: List<POSOrderItemEntity> = emptyList()
    private val _todayOrders = MutableStateFlow(0)
    val todayOrders: StateFlow<Int> = _todayOrders

    // --- TABS & HISTORY STATE ---
    private val _selectedTab = MutableStateFlow(0) // 0 = Today, 1 = History
    val selectedTab: StateFlow<Int> = _selectedTab

    // Orders List (Reused for both tabs)
    private val _displayedOrders = MutableStateFlow<List<POSOrderEntity>>(emptyList())
    val displayedOrders: StateFlow<List<POSOrderEntity>> = _displayedOrders

    // Date Selection for "History" Tab
    private val _selectedHistoryDate = MutableStateFlow<Long?>(null) // Null means "Select Date"
    val selectedHistoryDate: StateFlow<Long?> = _selectedHistoryDate

    private var currentDiscountRatio: Double = 0.0

    fun syncOrders() {
        viewModelScope.launch {
            // 1. Sync Cloud to Local
            posRepository.syncPOSOrders()
            promotionRepository.syncPromotions()

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

    private val _posHistory = MutableStateFlow<List<POSOrderEntity>>(emptyList())
    val posHistory: StateFlow<List<POSOrderEntity>> = _posHistory

    private val _selectedOrderItems = MutableStateFlow<List<POSOrderItemEntity>>(emptyList())
    val selectedOrderItems: StateFlow<List<POSOrderItemEntity>> = _selectedOrderItems

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage

    private val _editingItems = MutableStateFlow<List<POSOrderItemEntity>>(emptyList())
    val editingItems: StateFlow<List<POSOrderItemEntity>> = _editingItems

    private val _currentEditingOrder = MutableStateFlow<POSOrderEntity?>(null)
    val currentEditingOrder: StateFlow<POSOrderEntity?> = _currentEditingOrder

    // Calculated totals for the edit screen
    private val _editTotal = MutableStateFlow(0.0)
    val editTotal: StateFlow<Double> = _editTotal

    private val _editDiscount = MutableStateFlow(0.0)
    val editDiscount: StateFlow<Double> = _editDiscount

    private val _editGrandTotal = MutableStateFlow(0.0)
    val editGrandTotal: StateFlow<Double> = _editGrandTotal


    fun loadOrderDetails(orderId: Long) {
        viewModelScope.launch {
            val order = posRepository.getPOSOrderById(orderId)
            _currentEditingOrder.value = order

            val items = posRepository.getPOSOrderItems(orderId)
            originalItemsList = items
            _editingItems.value = items

            // 1. CALCULATE RATIO from Original Order
            // Ratio = Discount / Subtotal
            val total = order?.totalAmount ?: 0.0
            val discount = order?.discount ?: 0.0

            currentDiscountRatio = if (total > 0) discount / total else 0.0

            _editDiscount.value = discount

            // Calculate initial totals without changing discount amount yet
            calculateEditTotals(updateDiscount = false)
        }
    }

    fun updateDiscount(amount: Double) {
        _editDiscount.value = amount

        // Update the ratio based on the new manual value
        // So future +/- clicks will follow this NEW ratio
        val currentSubTotal = _editTotal.value
        if (currentSubTotal > 0) {
            currentDiscountRatio = amount / currentSubTotal
        }

        calculateEditTotals(updateDiscount = false)
    }

    // Increase/Decrease Quantity
    fun updateItemQty(item: POSOrderItemEntity, delta: Int) {
        val currentList = _editingItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.variantSku == item.variantSku && it.size == item.size }

        if (index != -1) {
            val currentItem = currentList[index]
            val newQty = currentItem.quantity + delta

            if (newQty > 0) {
                currentList[index] = currentItem.copy(quantity = newQty)
                _editingItems.value = currentList

                // Pass TRUE to update discount automatically
                calculateEditTotals(updateDiscount = true)
            }
        }
    }

    fun removeItem(item: POSOrderItemEntity) {
        val currentList = _editingItems.value.toMutableList()
        currentList.removeIf { it.variantSku == item.variantSku && it.size == item.size }
        _editingItems.value = currentList

        // Pass TRUE to update discount automatically
        calculateEditTotals(updateDiscount = true)
    }

    private fun calculateEditTotals(updateDiscount: Boolean = true) {
        val subTotal = _editingItems.value.sumOf { it.price * it.quantity }
        _editTotal.value = subTotal

        // 2. AUTO-CALCULATE Discount based on Ratio
        if (updateDiscount) {
            val autoDiscount = subTotal * currentDiscountRatio
            // Round to 2 decimal places to avoid numbers like 10.0000001
            val roundedDiscount = autoDiscount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            _editDiscount.value = roundedDiscount
        }

        // 3. Calculate Grand Total
        val discount = _editDiscount.value
        val validDiscount = if (discount > subTotal) subTotal else discount
        _editGrandTotal.value = (subTotal - validDiscount).coerceAtLeast(0.0)
    }

    fun saveOrderChanges(order: POSOrderEntity, email: String, payment: String, status: String) {
        viewModelScope.launch {
            val currentItems = _editingItems.value
            if (currentItems.isEmpty()) {
                _updateMessage.value = "Error: Order must have at least one item."
                return@launch
            }

            // Use the values from our StateFlows
            val newSubTotal = _editTotal.value
            val newDiscount = _editDiscount.value // User edited value
            val newGrandTotal = _editGrandTotal.value

            val updatedOrder = order.copy(
                customerEmail = email,
                paymentMethod = payment,
                status = status,
                totalAmount = newSubTotal,
                discount = newDiscount,
                grandTotal = newGrandTotal
            )

            // ... existing DB update logic ...
            val result = posRepository.updatePOSOrderWithItems(updatedOrder, currentItems, originalItemsList)

            if (result.isSuccess) {
                // ... existing email logic ...
                if (email.isNotBlank()) {
                    try {
                        val receiptItems = posRepository.getReceiptItems(currentItems)
                        receiptRepository.triggerEmail(
                            toEmail = email,
                            orderId = "POS-${order.id} (Updated)",
                            customerName = "Customer",
                            items = receiptItems,
                            subTotal = newSubTotal,
                            deliveryFee = 0.0,
                            discountAmount = newDiscount
                        )
                        _updateMessage.value = "Order Updated & Receipt Sent"
                    } catch (e: Exception) {
                        _updateMessage.value = "Order Updated, Email Failed: ${e.message}"
                    }
                } else {
                    _updateMessage.value = "Order Updated Successfully"
                }

                // Refresh the list to reflect changes
                refreshCurrentList()
            } else {
                _updateMessage.value = "Update Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearMessage() {
        _updateMessage.value = null
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
        if (index == 0) {
            loadOrdersForDate(Date()) // Load Today
        } else {
            // If switching to History, check if a date was previously selected, else clear list
            val dateMillis = _selectedHistoryDate.value
            if (dateMillis != null) {
                loadOrdersForDate(Date(dateMillis))
            } else {
                _displayedOrders.value = emptyList()
            }
        }
    }

    fun selectHistoryDate(dateMillis: Long) {
        _selectedHistoryDate.value = dateMillis
        loadOrdersForDate(Date(dateMillis))
    }

    private fun loadOrdersForDate(date: Date) {
        viewModelScope.launch {
            _displayedOrders.value = posRepository.getOrdersForDate(date)
        }
    }

    // Refresh current view (called after edit/update)
    fun refreshCurrentList() {
        if (_selectedTab.value == 0) {
            loadOrdersForDate(Date())
        } else {
            _selectedHistoryDate.value?.let { loadOrdersForDate(Date(it)) }
        }
    }
}

class SalesHistoryViewModelFactory(
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesHistoryViewModel(posRepository, promotionRepository, receiptRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}