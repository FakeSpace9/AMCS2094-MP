package com.example.miniproject.viewmodel

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import com.example.miniproject.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddToCartStatus{
    object Idle : AddToCartStatus()
    object Loading : AddToCartStatus()
    object Success : AddToCartStatus()
    data class Error(val message: String) : AddToCartStatus()
}
class ProductDetailScreenViewModel (
    private val productDao: ProductDao,
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _product = MutableStateFlow<ProductEntity?>(null)
    val product: StateFlow<ProductEntity?> = _product

    private val _variants = MutableStateFlow<List<ProductVariantEntity>>(emptyList())
    val variants: StateFlow<List<ProductVariantEntity>> = _variants

    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize

    private val _sizeOrder = listOf("XS","S","M","L","XL")
    val availableSizes: StateFlow<List<String>> = MutableStateFlow(emptyList())

    private val _selectedVariant = MutableStateFlow<ProductVariantEntity?>(null)
    val selectedVariant: StateFlow<ProductVariantEntity?> = _selectedVariant

    private val _addToCartStatus = MutableStateFlow<AddToCartStatus>(AddToCartStatus.Idle)
    val addToCartStatus : StateFlow<AddToCartStatus> = _addToCartStatus

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
                updateSelectedVariant()
            }else if(_selectedSize.value.isNotEmpty()){
                updateSelectedVariant()

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

    private fun updateSelectedVariant() {
        val currentSize = _selectedSize.value
        val currentVariants = _variants.value

        _selectedVariant.value = currentVariants.find { it.size == currentSize }
    }
    fun addToCart(){
        val productVal = _product.value ?: return
        val variantVal = _selectedVariant.value ?: return

        if(_selectedSize.value.isEmpty()){
            _addToCartStatus.value = AddToCartStatus.Error("Please select a size")
            return
        }

        if(variantVal.stockQuantity <=0 ){
            _addToCartStatus.value = AddToCartStatus.Error("Out of Stock")
        }

        viewModelScope.launch {
            _addToCartStatus.value = AddToCartStatus.Loading
            try {
                val cartItem = CartEntity(
                    productId = productVal.productId,
                    variantSku = variantVal.sku,
                    productName = productVal.name,
                    productImageUrl = productVal.imageUrl,
                    selectedSize = _selectedSize.value,
                    selectedColour = variantVal.colour,
                    price = variantVal.price,
                    quantity = 1
                )
                cartRepository.addCartItem(cartItem)
                _addToCartStatus.value = AddToCartStatus.Success
            }catch (e:Exception){
                _addToCartStatus.value = AddToCartStatus.Error(e.message ?: "Failed to add to cart")
            }
        }
    }

    fun resetAddToCartStatus(){
        _addToCartStatus.value = AddToCartStatus.Idle
    }
}

