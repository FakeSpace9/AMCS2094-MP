package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductSearchViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ProductSearchResult>>(emptyList())
    val searchResults: StateFlow<List<ProductSearchResult>> = _searchResults

    val searchQuery = MutableStateFlow("")
    val selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedCategory = MutableStateFlow("All")

    // Static categories list
    private val allCategories = listOf("Tops", "Bottoms", "Outerwear", "Dresses", "Accessories", "Shoes")

    fun getAvailableCategories(): List<String> = listOf("All") + allCategories

    fun loadProducts() {
        viewModelScope.launch {
            // 1. Show cached data from Room immediately (Fast)
            updateListFromLocal()

            // 2. Trigger background sync (Firestore -> Room)
            repository.syncProducts()

            // 3. Refresh list to show any new changes from the sync
            updateListFromLocal()
        }
    }

    private suspend fun updateListFromLocal() {
        val query = searchQuery.value.trim()

        // Get raw list from Repository (which gets it from Room)
        var list = repository.searchProductsLocal(query)

        // Apply Memory Filters (Category)
        if (selectedCategory.value != "All") {
            list = list.filter {
                it.product.category.equals(selectedCategory.value, ignoreCase = true)
            }
        }

        // Apply Sorting
        list = when (selectedSort.value) {
            SortOption.NEWEST -> list // Default order
            SortOption.NAME_A_Z -> list.sortedBy { it.product.name }
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.minPrice }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.minPrice }
        }

        _searchResults.value = list
    }
}