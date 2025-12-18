package com.example.miniproject.viewmodel

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
    private val _productImages = MutableStateFlow<List<String>>(emptyList())
    val productImages: StateFlow<List<String>> = _productImages
    private val _product = MutableStateFlow<ProductEntity?>(null)
    val product: StateFlow<ProductEntity?> = _product

    private val _variants = MutableStateFlow<List<ProductVariantEntity>>(emptyList())
    val variants: StateFlow<List<ProductVariantEntity>> = _variants

    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize

    private val _sizeOrder = listOf("XS","S","M","L","XL","XXL")
    val availableSizes: StateFlow<List<String>> = MutableStateFlow(emptyList())

    private val _selectedVariant = MutableStateFlow<ProductVariantEntity?>(null)
    val selectedVariant: StateFlow<ProductVariantEntity?> = _selectedVariant

    private val _addToCartStatus = MutableStateFlow<AddToCartStatus>(AddToCartStatus.Idle)
    val addToCartStatus : StateFlow<AddToCartStatus> = _addToCartStatus

    val priceRange: StateFlow<String> = MutableStateFlow("")

    fun loadProductData(productId: String) {

        _product.value = null
        _variants.value = emptyList()
        _selectedVariant.value = null
        _selectedSize.value = "" // Reset size so it doesn't carry over
        (availableSizes as MutableStateFlow).value = emptyList()

        viewModelScope.launch {
            val productResult = productDao.getProductById(productId)
            _product.value = productResult
            if (productResult != null) {
                val imagesResult = productDao.getImagesForProduct(productId)
                val urls = imagesResult.map { it.imageUrl }

                // If gallery images exist, use them. Otherwise, use the main image.
                if (urls.isNotEmpty()) {
                    _productImages.value = urls
                } else if (productResult.imageUrl.isNotEmpty()) {
                    _productImages.value = listOf(productResult.imageUrl)
                }
            }

            val variantsResult = productDao.getVariantsForProduct(productId)
            _variants.value = variantsResult

            val uniqueSizes = variantsResult.map { it.size }
                .distinct()
                .filter { it.isNotEmpty() }
                .sortedBy { size ->
                    val index = _sizeOrder.indexOf(size)
                    // If a size is not in the list (index -1), put it at the end
                    if (index == -1) Int.MAX_VALUE else index
                }
            (availableSizes as MutableStateFlow).value = uniqueSizes

            if (uniqueSizes.isNotEmpty()) {
                _selectedSize.value = uniqueSizes.first()
                updateSelectedVariant()
            } else {
                _selectedSize.value = ""
                _selectedVariant.value = null
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
        updateSelectedVariant()
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

