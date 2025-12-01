package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.data.entity.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductSearchViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ProductSearchResult>>(emptyList())
    val searchResults: StateFlow<List<ProductSearchResult>> = _searchResults

    val searchQuery = MutableStateFlow("")
    val selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedCategory = MutableStateFlow("All")

    private val allCategories = listOf("Tops", "Bottoms", "Outerwear", "Dresses", "Accessories", "Shoes")

    fun getAvailableCategories(): List<String> = listOf("All") + allCategories

    fun loadProducts() {
        viewModelScope.launch {
            try {
                // 1. Fetch all products from Firestore
                val snapshot = firestore.collection("products").get().await()

                // 2. Map to ProductSearchResult
                val allProducts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("productId") ?: return@mapNotNull null
                        val name = doc.getString("name") ?: ""
                        val desc = doc.getString("description") ?: ""
                        val cat = doc.getString("category") ?: ""
                        val gend = doc.getString("gender") ?: ""
                        val img = doc.getString("imageUrl") ?: ""

                        val variantsList = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()

                        var totalStock = 0
                        var minPrice = Double.MAX_VALUE
                        var maxPrice = 0.0
                        var firstSku = ""

                        val parsedVariants = variantsList.mapIndexed { index, v ->
                            val qty = (v["qty"] as? Long)?.toInt() ?: 0
                            val price = (v["price"] as? Number)?.toDouble() ?: 0.0
                            val sku = (v["sku"] as? String) ?: ""
                            val size = (v["size"] as? String) ?: ""
                            val colour = (v["colour"] as? String) ?: ""

                            if (index == 0) firstSku = sku
                            totalStock += qty
                            if (price < minPrice) minPrice = price
                            if (price > maxPrice) maxPrice = price

                            VariantUiState(
                                id = UUID.randomUUID().toString(),
                                size = size,
                                colour = colour,
                                sku = sku,
                                price = price.toString(),
                                quantity = qty.toString()
                            )
                        }

                        if (minPrice == Double.MAX_VALUE) minPrice = 0.0

                        ProductSearchResult(
                            product = ProductEntity(id, name, desc, cat, gend, img),
                            totalStock = totalStock,
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            displaySku = firstSku
                        ).apply {
                            variants = parsedVariants
                        }
                    } catch (e: Exception) { null }
                }

                // 3. Filter & Sort
                var filteredList = allProducts
                val query = searchQuery.value.lowercase().trim()

                if (query.isNotBlank()) {
                    filteredList = filteredList.filter { result ->
                        val nameMatch = result.product.name.lowercase().contains(query)
                        val catMatch = result.product.category.lowercase().contains(query)
                        val skuMatch = result.variants.any { it.sku.lowercase().contains(query) }
                        nameMatch || catMatch || skuMatch
                    }
                }

                if (selectedCategory.value != "All") {
                    filteredList = filteredList.filter { it.product.category.equals(selectedCategory.value, ignoreCase = true) }
                }

                filteredList = when (selectedSort.value) {
                    SortOption.NEWEST -> filteredList
                    SortOption.NAME_A_Z -> filteredList.sortedBy { it.product.name }
                    SortOption.PRICE_LOW_HIGH -> filteredList.sortedBy { it.minPrice }
                    SortOption.PRICE_HIGH_LOW -> filteredList.sortedByDescending { it.minPrice }
                }

                _searchResults.value = filteredList

            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            }
        }
    }
}