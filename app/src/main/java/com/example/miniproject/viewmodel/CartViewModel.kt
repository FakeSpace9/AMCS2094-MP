package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository
) : ViewModel() {

    // All items from DB
    val cartItems: StateFlow<List<CartEntity>> = repository.allCartItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // State for selected item IDs
    private val _selectedItemIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItemIds: StateFlow<Set<Int>> = _selectedItemIds

    // Calculate total ONLY for selected items (Excluding shipping)
    val selectedTotal: StateFlow<Double> = combine(cartItems, _selectedItemIds) { items, selectedIds ->
        items.filter { it.id in selectedIds }.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun toggleSelection(itemId: Int) {
        val current = _selectedItemIds.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _selectedItemIds.value = current
    }

    fun selectAll() {
        val allIds = cartItems.value.map { it.id }.toSet()
        _selectedItemIds.value = allIds
    }

    fun deselectAll() {
        _selectedItemIds.value = emptySet()
    }

    fun updateQuantity(item: CartEntity, newQuantity: Int){
        viewModelScope.launch {
            repository.updateQuantity(item, newQuantity)
        }
    }

    fun removeFromCart(item: CartEntity){
        viewModelScope.launch {
            repository.removeCartItem(item)
            // Also remove from selection if present
            val current = _selectedItemIds.value.toMutableSet()
            if(current.contains(item.id)){
                current.remove(item.id)
                _selectedItemIds.value = current
            }
        }
    }
}