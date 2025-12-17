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

// --- UI Wrapper to hold Entity + Image ---
data class EditedItemUiState(
    val data: POSOrderItemEntity,
    val imageUrl: String
)

class SalesHistoryViewModel(
    private val posRepository: POSRepository,
    private val promotionRepository: PromotionRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    // --- Stats State ---
    private val _todaySales = MutableStateFlow(0.0)
    val todaySales: StateFlow<Double> = _todaySales

    private val _todayOrders = MutableStateFlow(0)
    val todayOrders: StateFlow<Int> = _todayOrders

    // --- TABS & HISTORY STATE ---
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _displayedOrders = MutableStateFlow<List<POSOrderEntity>>(emptyList())
    val displayedOrders: StateFlow<List<POSOrderEntity>> = _displayedOrders

    private val _selectedHistoryDate = MutableStateFlow<Long?>(null)
    val selectedHistoryDate: StateFlow<Long?> = _selectedHistoryDate

    private var originalItemsList: List<POSOrderItemEntity> = emptyList()
    private var currentDiscountRatio: Double = 0.0

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage

    // --- CHANGED: Now holds EditedItemUiState instead of raw Entity ---
    private val _editingItems = MutableStateFlow<List<EditedItemUiState>>(emptyList())
    val editingItems: StateFlow<List<EditedItemUiState>> = _editingItems

    private val _currentEditingOrder = MutableStateFlow<POSOrderEntity?>(null)
    val currentEditingOrder: StateFlow<POSOrderEntity?> = _currentEditingOrder

    private val _editTotal = MutableStateFlow(0.0)
    val editTotal: StateFlow<Double> = _editTotal

    private val _editDiscount = MutableStateFlow(0.0)
    val editDiscount: StateFlow<Double> = _editDiscount

    private val _editGrandTotal = MutableStateFlow(0.0)
    val editGrandTotal: StateFlow<Double> = _editGrandTotal


    fun syncOrders() {
        viewModelScope.launch {
            posRepository.syncPOSOrders()
            promotionRepository.syncPromotions()
            loadDashboardStats()
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _todaySales.value = posRepository.getTodaySales()
            _todayOrders.value = posRepository.getTodayOrderCount()
        }
    }

    fun loadOrderDetails(orderId: Long) {
        viewModelScope.launch {
            val order = posRepository.getPOSOrderById(orderId)
            _currentEditingOrder.value = order

            // 1. Fetch raw items
            val rawItems = posRepository.getPOSOrderItems(orderId)
            originalItemsList = rawItems

            // 2. Fetch images and map to UI State
            val uiItems = rawItems.map { item ->
                val product = posRepository.getProductById(item.productId)
                EditedItemUiState(item, product?.imageUrl ?: "")
            }
            _editingItems.value = uiItems

            // 3. Calculate Ratio
            val total = order?.totalAmount ?: 0.0
            val discount = order?.discount ?: 0.0
            currentDiscountRatio = if (total > 0) discount / total else 0.0

            _editDiscount.value = discount
            calculateEditTotals(updateDiscount = false)
        }
    }

    fun updateDiscount(amount: Double) {
        _editDiscount.value = amount
        val currentSubTotal = _editTotal.value
        if (currentSubTotal > 0) {
            currentDiscountRatio = amount / currentSubTotal
        }
        calculateEditTotals(updateDiscount = false)
    }

    fun updateItemQty(itemUi: EditedItemUiState, delta: Int) {
        val currentList = _editingItems.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.data.variantSku == itemUi.data.variantSku && it.data.size == itemUi.data.size
        }

        if (index != -1) {
            val currentUiItem = currentList[index]
            val newQty = currentUiItem.data.quantity + delta

            if (newQty > 0) {
                // Update the Entity inside the UI Wrapper
                val newEntity = currentUiItem.data.copy(quantity = newQty)
                currentList[index] = currentUiItem.copy(data = newEntity)

                _editingItems.value = currentList
                calculateEditTotals(updateDiscount = true)
            }
        }
    }

    fun removeItem(itemUi: EditedItemUiState) {
        val currentList = _editingItems.value.toMutableList()
        currentList.removeIf {
            it.data.variantSku == itemUi.data.variantSku && it.data.size == itemUi.data.size
        }
        _editingItems.value = currentList
        calculateEditTotals(updateDiscount = true)
    }

    private fun calculateEditTotals(updateDiscount: Boolean = true) {
        // Sum using item.data
        val subTotal = _editingItems.value.sumOf { it.data.price * it.data.quantity }
        _editTotal.value = subTotal

        if (updateDiscount) {
            val autoDiscount = subTotal * currentDiscountRatio
            val roundedDiscount = autoDiscount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            _editDiscount.value = roundedDiscount
        }

        val discount = _editDiscount.value
        val validDiscount = if (discount > subTotal) subTotal else discount
        _editGrandTotal.value = (subTotal - validDiscount).coerceAtLeast(0.0)
    }

    fun saveOrderChanges(order: POSOrderEntity, email: String, payment: String, status: String) {
        viewModelScope.launch {
            val currentUiItems = _editingItems.value
            if (currentUiItems.isEmpty()) {
                _updateMessage.value = "Error: Order must have at least one item."
                return@launch
            }

            // Extract Entities back from UI State
            val currentItems = currentUiItems.map { it.data }

            val newSubTotal = _editTotal.value
            val newDiscount = _editDiscount.value
            val newGrandTotal = _editGrandTotal.value

            val updatedOrder = order.copy(
                customerEmail = email,
                paymentMethod = payment,
                status = status,
                totalAmount = newSubTotal,
                discount = newDiscount,
                grandTotal = newGrandTotal
            )

            val result = posRepository.updatePOSOrderWithItems(updatedOrder, currentItems, originalItemsList)

            if (result.isSuccess) {
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
                refreshCurrentList()
            } else {
                _updateMessage.value = "Update Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearMessage() { _updateMessage.value = null }

    fun setTab(index: Int) {
        _selectedTab.value = index
        if (index == 0) {
            loadOrdersForDate(Date())
        } else {
            val dateMillis = _selectedHistoryDate.value
            if (dateMillis != null) loadOrdersForDate(Date(dateMillis)) else _displayedOrders.value = emptyList()
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

    fun refreshCurrentList() {
        if (_selectedTab.value == 0) loadOrdersForDate(Date())
        else _selectedHistoryDate.value?.let { loadOrdersForDate(Date(it)) }
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