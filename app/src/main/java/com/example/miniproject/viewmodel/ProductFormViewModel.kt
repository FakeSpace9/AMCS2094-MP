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

    // --- FORM DATA STATE ---
    var currentProductId: String? = null
    var productName = MutableStateFlow("")
    var productDesc = MutableStateFlow("")
    var category = MutableStateFlow("")
    var gender = MutableStateFlow("")

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _existingImageUrls = MutableStateFlow<List<String>>(emptyList())
    val existingImageUrls: StateFlow<List<String>> = _existingImageUrls

    // --- NEW: Track initial images to detect deletions ---
    private var initialImageUrls: List<String> = emptyList()

    private val _variants = MutableStateFlow<List<VariantUiState>>(listOf(VariantUiState()))
    val variants: StateFlow<List<VariantUiState>> = _variants

    private val _saveState = MutableStateFlow<ProductState>(ProductState.Idle)
    val saveState: StateFlow<ProductState> = _saveState

    // --- VALIDATION STATE ---
    private val _takenSkus = MutableStateFlow<List<String>>(emptyList())
    val takenSkus: StateFlow<List<String>> = _takenSkus

    val allSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    val allCategories = listOf("Tops", "Bottoms", "Outerwear", "Dresses", "Accessories", "Shoes")
    val allGenders = listOf("Unisex", "Male", "Female")
    val validColors = listOf(
        "Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink",
        "Brown", "Grey", "Beige", "Navy", "Maroon", "Teal", "Olive", "Gold", "Silver", "Multi"
    )

    // --- SAVE LOGIC ---
    fun saveProduct() {
        viewModelScope.launch {
            _saveState.value = ProductState.Loading

            // 1. Basic Validation
            if (productName.value.isBlank() || category.value.isBlank() || gender.value.isBlank() || _variants.value.isEmpty()) {
                _saveState.value = ProductState.Error("Please fill all required fields")
                return@launch
            }

            // 2. Size Validation
            if (_variants.value.any { it.size.isBlank() }) {
                _saveState.value = ProductState.Error("Please select a size for all variants.")
                return@launch
            }

            // 3. Color Validation
            if (_variants.value.any { v -> validColors.none { it.equals(v.colour, true) } }) {
                _saveState.value = ProductState.Error("Invalid color selected.")
                return@launch
            }

            // 4. Price/Stock Validation
            if (_variants.value.any { (it.price.toDoubleOrNull() ?: 0.0) <= 0.0 }) {
                _saveState.value = ProductState.Error("Price cannot be 0.")
                return@launch
            }
            if (_variants.value.any { (it.quantity.toIntOrNull() ?: -1) < 0 }) {
                _saveState.value = ProductState.Error("Stock cannot be negative.")
                return@launch
            }

            // 5. SKU Validation (Strict & Normalized Uppercase)
            val currentFormSkus = _variants.value.map { it.sku.trim().uppercase() }

            // A. Check Duplicates in form
            if (currentFormSkus.size != currentFormSkus.distinct().size) {
                _saveState.value = ProductState.Error("Duplicate SKUs found in the form.")
                return@launch
            }

            // B. Check against Database (Using cached list)
            if (currentFormSkus.any { it in _takenSkus.value }) {
                _saveState.value = ProductState.Error("One or more SKUs already exist in database!")
                return@launch
            }

            try {
                // --- FIX: DETECT AND DELETE REMOVED IMAGES (Edit Mode) ---
                if (currentProductId != null) {
                    val keptImages = _existingImageUrls.value.toSet()
                    // Find images that were in initial list but NOT in the final kept list
                    val imagesToDelete = initialImageUrls.filter { it !in keptImages }

                    imagesToDelete.forEach { url ->
                        try {
                            storage.getReferenceFromUrl(url).delete().await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                // ---------------------------------------------------------

                val productId = currentProductId ?: UUID.randomUUID().toString()

                // Upload NEW Images
                val finalImageUrls = _existingImageUrls.value.toMutableList()
                for ((index, uri) in _selectedImages.value.withIndex()) {
                    val unique = "${productId}_${System.currentTimeMillis()}_$index.jpg"
                    val ref = storage.reference.child("product_images/$unique")
                    ref.putFile(uri).await()
                    finalImageUrls.add(ref.downloadUrl.await().toString())
                }
                val cover = finalImageUrls.firstOrNull() ?: ""

                // Save to Room
                val productEntity = ProductEntity(
                    productId,
                    productName.value,
                    productDesc.value,
                    category.value,
                    gender.value,
                    cover
                )
                val imageEntities =
                    finalImageUrls.map { ProductImageEntity(productId = productId, imageUrl = it) }
                val variantEntities = _variants.value.map { ui ->
                    ProductVariantEntity(
                        0, productId, ui.size, ui.colour,
                        ui.sku.trim().uppercase(), // Ensure saved as Uppercase
                        ui.price.toDoubleOrNull() ?: 0.0, ui.quantity.toIntOrNull() ?: 0
                    )
                }

                productDao.insertProduct(productEntity)
                productDao.insertImages(imageEntities)
                productDao.insertVariants(variantEntities)

                // Save to Firestore
                val productData = mapOf(
                    "productId" to productId,
                    "name" to productName.value,
                    "description" to productDesc.value,
                    "category" to category.value,
                    "gender" to gender.value,
                    "imageUrl" to cover,
                    "images" to finalImageUrls,
                    "variants" to variantEntities.map {
                        mapOf(
                            "sku" to it.sku,
                            "size" to it.size,
                            "colour" to it.colour,
                            "qty" to it.stockQuantity,
                            "price" to it.price
                        )
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
            resetState(fetchSkus = false) // Don't fetch yet, wait until ID is set
            currentProductId = product.product.productId

            productName.value = product.product.name
            productDesc.value = product.product.description
            category.value = product.product.category
            gender.value = product.product.gender

            try {
                val doc =
                    firestore.collection("products").document(currentProductId!!).get().await()
                var imagesList = doc.get("images") as? List<String> ?: emptyList()
                if (imagesList.isEmpty()) {
                    val singleUrl = doc.getString("imageUrl")
                    if (!singleUrl.isNullOrBlank()) imagesList = listOf(singleUrl)
                }
                _existingImageUrls.value = imagesList

                // --- FIX: Store initial state for comparison later ---
                initialImageUrls = imagesList
                // ----------------------------------------------------

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

            fetchTakenSkus() // Refresh "Taken List" ignoring current product
        }
    }

    // --- HELPER FUNCTIONS ---

    fun resetState(fetchSkus: Boolean = true) {
        currentProductId = null
        initialImageUrls = emptyList() // Reset initial list
        _saveState.value = ProductState.Idle
        productName.value = ""
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
                    if (doc.id == currentProductId) continue // Skip own SKUs if editing
                    val vars = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                    vars.forEach { v ->
                        val s = v["sku"] as? String
                        if (!s.isNullOrBlank()) skus.add(s.trim().uppercase())
                    }
                }
                _takenSkus.value = skus
            } catch (e: Exception) {
            }
        }
    }

    fun onImagesSelected(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value + uris
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun removeExistingImage(url: String) {
        _existingImageUrls.value = _existingImageUrls.value - url
    }

    fun addVariantCard() {
        _variants.value = _variants.value + VariantUiState()
    }

    fun removeVariantCard(id: String) {
        if (_variants.value.size > 1) _variants.value = _variants.value.filter { it.id != id }
    }

    fun updateVariant(id: String, update: (VariantUiState) -> Unit) {
        val list = _variants.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = list[index].copy()
            update(item)
            list[index] = item
            _variants.value = list
        }
    }

    fun getUnavailableSizes(currentId: String, currentColor: String): List<String> {
        return _variants.value.filter {
            it.id != currentId && it.colour.equals(
                currentColor,
                true
            ) && it.size.isNotBlank()
        }.map { it.size }
    }

    fun deleteProduct() {
        val id = currentProductId ?: return
        viewModelScope.launch {
            _saveState.value = ProductState.Loading
            try {
                // Delete Images from Firebase Storage
                val imagesToDelete = _existingImageUrls.value
                imagesToDelete.forEach { url ->
                    try {
                        val ref = storage.getReferenceFromUrl(url)
                        ref.delete().await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Delete from Room
                productDao.deleteProduct(id)

                // Delete from Firestore
                firestore.collection("products").document(id).delete().await()

                _saveState.value = ProductState.Success
            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Delete failed")
            }
        }
    }
}