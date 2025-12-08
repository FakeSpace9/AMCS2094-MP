package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailScreenViewModel (
    private val productDao: ProductDao
) : ViewModel() {
    private val _product = MutableStateFlow<ProductEntity?>(null)
    val product: StateFlow<ProductEntity?> = _product

    private val _variants = MutableStateFlow<List<ProductVariantEntity>>(emptyList())
    val variants: StateFlow<List<ProductVariantEntity>> = _variants

    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize

    private val _sizeOrder = listOf("XS","S","M","L","XL")
    val availableSizes: StateFlow<List<String>> = MutableStateFlow(emptyList())

    val priceRange: StateFlow<String> = MutableStateFlow("")

    fun loadProductData(productId: String) {
        viewModelScope.launch {
            val productResult = productDao.getProductById(productId)
            _product.value = productResult

            val variantsResult = productDao.getVariantsForProduct(productId)
            _variants.value = variantsResult

            val uniqueSizes = variantsResult.map { it.size }
                .distinct()
                .filter { it.isNotEmpty() }
                .sortedBy { _sizeOrder.indexOf(it) }
            (availableSizes as MutableStateFlow).value = uniqueSizes

            if(_selectedSize.value.isEmpty()&&uniqueSizes.isNotEmpty()){
                _selectedSize.value = uniqueSizes.first()
            }

            if(variantsResult.isNotEmpty()){
                val minPrice = variantsResult.minOf { it.price }
                val maxPrice = variantsResult.maxOf { it.price }
                val rangeStr = if (minPrice != maxPrice) {
                    "RM${minPrice} - RM${maxPrice}"
                } else {
                    "RM${minPrice}"
                }
                (priceRange as MutableStateFlow).value = rangeStr
            }
        }
    }

    fun selectSize(size:String){
        _selectedSize.value = size
    }
}

