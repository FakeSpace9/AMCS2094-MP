package com.example.miniproject.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductImageEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductFormViewModel(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val productDao: ProductDao
) : ViewModel() {
    var currentProductId: String? = null

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName

    var productDesc = MutableStateFlow("")
    var category = MutableStateFlow("")
    var gender = MutableStateFlow("")

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _existingImageUrls = MutableStateFlow<List<String>>(emptyList())
    val existingImageUrls: StateFlow<List<String>> = _existingImageUrls

    private var initialImageUrls: List<String> = emptyList()

    private val _variants = MutableStateFlow<List<VariantUiState>>(listOf(VariantUiState()))
    val variants: StateFlow<List<VariantUiState>> = _variants

    private val _saveState = MutableStateFlow<ProductState>(ProductState.Idle)
    val saveState: StateFlow<ProductState> = _saveState

    // --- VALIDATION STATE ---
    private val _takenSkus = MutableStateFlow<List<String>>(emptyList())
    val takenSkus: StateFlow<List<String>> = _takenSkus

    val allSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    val allCategories = listOf("Tops", "Bottoms", "Outerwear", "Dresses", "Accessories")
    val allGenders = listOf("Unisex", "Male", "Female")
    val validColors = listOf(
        "Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink",
        "Brown", "Grey", "Beige", "Navy", "Maroon", "Teal", "Olive", "Gold", "Silver", "Multi"
    )

    private fun generateSku(name: String, color: String, size: String): String {
        if (name.isBlank() || color.isBlank() || size.isBlank()) return ""

        val namePart = name.replace(" ", "").take(4).uppercase()
        val colorPart = color.take(3).uppercase()
        val sizePart = size.uppercase()

        return "$namePart-$colorPart-$sizePart"
    }

    fun updateProductName(newName: String) {
        _productName.value = newName

        val currentList = _variants.value.map { it.copy() }
        currentList.forEach { variant ->
            val newSku = generateSku(newName, variant.colour, variant.size)
            if (newSku.isNotBlank()) {
                variant.sku = newSku
            }
        }
        _variants.value = currentList
    }

    fun updateVariant(id: String, update: (VariantUiState) -> Unit) {
        val list = _variants.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            // 1. Create copy and apply updates
            val original = list[index]
            val updatedItem = original.copy()
            update(updatedItem)

            // 2. Check if critical fields changed
            val isColorChanged = original.colour != updatedItem.colour
            val isSizeChanged = original.size != updatedItem.size

            // 3. Auto-Generate SKU if Color or Size changed
            if (isColorChanged || isSizeChanged) {
                val newSku = generateSku(_productName.value, updatedItem.colour, updatedItem.size)
                // Only overwrite if we generated a valid SKU (i.e., all fields are present)
                // This allows the user to clear fields without setting a weird SKU
                if (newSku.isNotBlank()) {
                    updatedItem.sku = newSku
                } else if (updatedItem.colour.isBlank() || updatedItem.size.isBlank()) {
                    // Optionally clear SKU if fields are removed, or keep it.
                    // Let's clear it to avoid "JAC-BLU-XS" remaining when size is removed.
                    updatedItem.sku = ""
                }
            }

            list[index] = updatedItem
            _variants.value = list
        }
    }

    // --- SAVE LOGIC ---
    fun saveProduct() {
        viewModelScope.launch {
            _saveState.value = ProductState.Loading

            // 1. Basic Validation
            if (_productName.value.isBlank() || category.value.isBlank() || gender.value.isBlank() || _variants.value.isEmpty()) {
                _saveState.value = ProductState.Error("Please fill all required fields")
                return@launch
            }

            // ... (Rest of validation remains same) ...
            if (_variants.value.any { it.size.isBlank() }) {
                _saveState.value = ProductState.Error("Please select a size for all variants.")
                return@launch
            }
            if (_variants.value.any { v -> validColors.none { it.equals(v.colour, true) } }) {
                _saveState.value = ProductState.Error("Invalid color selected.")
                return@launch
            }
            if (_variants.value.any { (it.price.toDoubleOrNull() ?: 0.0) <= 0.0 }) {
                _saveState.value = ProductState.Error("Price cannot be 0.")
                return@launch
            }
            if (_variants.value.any { (it.quantity.toIntOrNull() ?: -1) < 0 }) {
                _saveState.value = ProductState.Error("Stock cannot be negative.")
                return@launch
            }

            val currentFormSkus = _variants.value.map { it.sku.trim().uppercase() }
            if (currentFormSkus.size != currentFormSkus.distinct().size) {
                _saveState.value = ProductState.Error("Duplicate SKUs found in the form.")
                return@launch
            }
            if (currentFormSkus.any { it in _takenSkus.value }) {
                _saveState.value = ProductState.Error("One or more SKUs already exist in database!")
                return@launch
            }

            try {
                if (currentProductId != null) {
                    val keptImages = _existingImageUrls.value.toSet()
                    val imagesToDelete = initialImageUrls.filter { it !in keptImages }
                    imagesToDelete.forEach { url ->
                        try {
                            storage.getReferenceFromUrl(url).delete().await()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                val productId = currentProductId ?: UUID.randomUUID().toString()

                val finalImageUrls = _existingImageUrls.value.toMutableList()
                for ((index, uri) in _selectedImages.value.withIndex()) {
                    val unique = "${productId}_${System.currentTimeMillis()}_$index.jpg"
                    val ref = storage.reference.child("product_images/$unique")
                    ref.putFile(uri).await()
                    finalImageUrls.add(ref.downloadUrl.await().toString())
                }
                val cover = finalImageUrls.firstOrNull() ?: ""

                val productEntity = ProductEntity(
                    productId, _productName.value, productDesc.value, category.value, gender.value, cover
                )
                val imageEntities = finalImageUrls.map { ProductImageEntity(productId = productId, imageUrl = it) }
                val variantEntities = _variants.value.map { ui ->
                    ProductVariantEntity(
                        0, productId, ui.size, ui.colour,
                        ui.sku.trim().uppercase(),
                        ui.price.toDoubleOrNull() ?: 0.0, ui.quantity.toIntOrNull() ?: 0
                    )
                }

                productDao.insertProduct(productEntity)
                productDao.insertImages(imageEntities)
                productDao.insertVariants(variantEntities)

                val productData = mapOf(
                    "productId" to productId,
                    "name" to _productName.value,
                    "description" to productDesc.value,
                    "category" to category.value,
                    "gender" to gender.value,
                    "imageUrl" to cover,
                    "images" to finalImageUrls,
                    "variants" to variantEntities.map {
                        mapOf("sku" to it.sku, "size" to it.size, "colour" to it.colour, "qty" to it.stockQuantity, "price" to it.price)
                    }
                )
                firestore.collection("products").document(productId).set(productData).await()

                _saveState.value = ProductState.Success

            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Error saving")
            }
        }
    }

    // --- EDIT MODE LOGIC ---
    fun loadProductForEdit(product: ProductSearchResult) {
        viewModelScope.launch {
            resetState(fetchSkus = false)
            currentProductId = product.product.productId

            _productName.value = product.product.name // Update local StateFlow
            productDesc.value = product.product.description
            category.value = product.product.category
            gender.value = product.product.gender

            try {
                val doc = firestore.collection("products").document(currentProductId!!).get().await()
                var imagesList = doc.get("images") as? List<String> ?: emptyList()
                if (imagesList.isEmpty()) {
                    val singleUrl = doc.getString("imageUrl")
                    if (!singleUrl.isNullOrBlank()) imagesList = listOf(singleUrl)
                }
                _existingImageUrls.value = imagesList
                initialImageUrls = imagesList

                val variantsList = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                if (variantsList.isNotEmpty()) {
                    _variants.value = variantsList.map { map ->
                        VariantUiState(
                            size = map["size"] as? String ?: "",
                            colour = map["colour"] as? String ?: "",
                            sku = map["sku"] as? String ?: "",
                            price = (map["price"] as? Number)?.toString() ?: "",
                            quantity = (map["qty"] as? Number)?.toString() ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                if (product.variants.isNotEmpty()) _variants.value = product.variants
            }
            fetchTakenSkus()
        }
    }

    fun resetState(fetchSkus: Boolean = true) {
        currentProductId = null
        initialImageUrls = emptyList()
        _saveState.value = ProductState.Idle
        _productName.value = ""
        productDesc.value = ""
        category.value = ""
        gender.value = ""
        _selectedImages.value = emptyList()
        _existingImageUrls.value = emptyList()
        _variants.value = listOf(VariantUiState())

        if (fetchSkus) fetchTakenSkus()
    }

    fun fetchTakenSkus() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("products").get().await()
                val skus = mutableListOf<String>()
                for (doc in snapshot.documents) {
                    if (doc.id == currentProductId) continue
                    val vars = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                    vars.forEach { v ->
                        val s = v["sku"] as? String
                        if (!s.isNullOrBlank()) skus.add(s.trim().uppercase())
                    }
                }
                _takenSkus.value = skus
            } catch (e: Exception) { }
        }
    }

    fun onImagesSelected(uris: List<Uri>) { _selectedImages.value = _selectedImages.value + uris }
    fun removeImage(uri: Uri) { _selectedImages.value = _selectedImages.value - uri }
    fun removeExistingImage(url: String) { _existingImageUrls.value = _existingImageUrls.value - url }
    fun addVariantCard() { _variants.value = _variants.value + VariantUiState() }
    fun removeVariantCard(id: String) { if (_variants.value.size > 1) _variants.value = _variants.value.filter { it.id != id } }

    fun getUnavailableSizes(currentId: String, currentColor: String): List<String> {
        return _variants.value.filter { it.id != currentId && it.colour.equals(currentColor, true) && it.size.isNotBlank() }.map { it.size }
    }

    fun deleteProduct() {
        val id = currentProductId ?: return
        viewModelScope.launch {
            _saveState.value = ProductState.Loading
            try {
                val imagesToDelete = _existingImageUrls.value
                imagesToDelete.forEach { url ->
                    try { storage.getReferenceFromUrl(url).delete().await() } catch (e: Exception) { }
                }
                productDao.deleteProduct(id)
                firestore.collection("products").document(id).delete().await()
                _saveState.value = ProductState.Success
            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Delete failed")
            }
        }
    }
}