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

// --- Helper Classes ---

// UI State for a single Variant Card
data class VariantUiState(
    val id: String = UUID.randomUUID().toString(),
    var size: String = "",
    var colour: String = "",
    var sku: String = "",
    var price: String = "",
    var quantity: String = ""
)

// UI State for Save/Load operations
sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    object Success : ProductState()
    data class Error(val message: String) : ProductState()
}

// Sort Options for Search
enum class SortOption {
    NEWEST,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    NAME_A_Z
}

// --- ViewModel ---

class AddProductViewModel(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val productDao: ProductDao
) : ViewModel() {

    // --- SEARCH & FILTER STATE ---
    private val _searchResults = MutableStateFlow<List<ProductSearchResult>>(emptyList())
    val searchResults: StateFlow<List<ProductSearchResult>> = _searchResults

    val searchQuery = MutableStateFlow("")
    val selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedCategory = MutableStateFlow("All")

    // --- FORM DATA STATE (Add/Edit) ---
    var currentProductId: String? = null // Null = Add Mode, Not Null = Edit Mode

    var productName = MutableStateFlow("")
    var productDesc = MutableStateFlow("")
    var category = MutableStateFlow("")
    var gender = MutableStateFlow("")

    // --- IMAGES STATE ---
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList()) // New images selected from gallery
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _existingImageUrls = MutableStateFlow<List<String>>(emptyList()) // Old images URLs from DB (Edit Mode)
    val existingImageUrls: StateFlow<List<String>> = _existingImageUrls

    // --- VARIANTS STATE ---
    private val _variants = MutableStateFlow<List<VariantUiState>>(listOf(VariantUiState()))
    val variants: StateFlow<List<VariantUiState>> = _variants

    // --- STATUS STATE ---
    private val _saveState = MutableStateFlow<ProductState>(ProductState.Idle)
    val saveState: StateFlow<ProductState> = _saveState

    // --- STATIC LISTS FOR DROPDOWNS ---
    val allSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    val allCategories = listOf("Tops", "Bottoms", "Outerwear", "Dresses", "Accessories", "Shoes")
    val allGenders = listOf("Unisex", "Male", "Female")

    // Valid Colors for Validation
    val validColors = listOf(
        "Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink",
        "Brown", "Grey", "Beige", "Navy", "Maroon", "Teal", "Olive", "Gold", "Silver", "Multi"
    )

    // SKU Validation State
    private val _takenSkus = MutableStateFlow<List<String>>(emptyList())
    val takenSkus: StateFlow<List<String>> = _takenSkus

    // ============================================================================================
    // 1. SEARCH & LOAD LOGIC (From Firestore)
    // ============================================================================================

    fun loadProducts() {
        viewModelScope.launch {
            try {
                // A. Fetch all products
                val snapshot = firestore.collection("products").get().await()

                // B. Map to ProductSearchResult
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
                        var firstSku = ""
                        var maxPrice = 0.0

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
                    } catch (e: Exception) {
                        null
                    }
                }

                // C. Filter & Sort Logic (FIXED)
                var filteredList = allProducts

                // 1. Filter by Search Query
                val query = searchQuery.value.lowercase().trim()
                if (query.isNotBlank()) {
                    filteredList = filteredList.filter { result ->
                        // Check Name
                        val nameMatch = result.product.name.lowercase().contains(query)
                        // Check Category
                        val catMatch = result.product.category.lowercase().contains(query)

                        // FIX: Check ALL Variant SKUs, not just the first one
                        val skuMatch = result.variants.any { variant ->
                            variant.sku.lowercase().contains(query)
                        }

                        nameMatch || catMatch || skuMatch
                    }
                }

                // 2. Filter by Category Dropdown
                if (selectedCategory.value != "All") {
                    filteredList = filteredList.filter {
                        it.product.category.equals(selectedCategory.value, ignoreCase = true)
                    }
                }

                // 3. Sort
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

    fun getAvailableCategories(): List<String> {
        return listOf("All") + allCategories
    }

    // ============================================================================================
    // 2. EDIT MODE LOGIC (Load Data)
    // ============================================================================================

    fun loadProductForEdit(product: ProductSearchResult) {
        viewModelScope.launch {
            resetState() // Clear form first
            currentProductId = product.product.productId

            // A. Fill Text Fields
            productName.value = product.product.name
            productDesc.value = product.product.description
            category.value = product.product.category
            gender.value = product.product.gender

            // B. Load Images (Fetch from Firestore to get latest list)
            try {
                val doc = firestore.collection("products").document(currentProductId!!).get().await()

                // Try to get 'images' list
                var imagesList = doc.get("images") as? List<String> ?: emptyList()

                // FALLBACK: If list is empty (old data), try to get single 'imageUrl'
                if (imagesList.isEmpty()) {
                    val singleUrl = doc.getString("imageUrl")
                    if (!singleUrl.isNullOrBlank()) {
                        imagesList = listOf(singleUrl)
                    }
                }
                _existingImageUrls.value = imagesList

                // C. Load Variants (From Firestore)
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
                } else {
                    // Fallback to Room if Firestore variants are empty
                    val dbVariants = productDao.getVariantsForProduct(currentProductId!!)
                    if (dbVariants.isNotEmpty()) {
                        _variants.value = dbVariants.map {
                            VariantUiState(
                                size = it.size,
                                colour = it.colour,
                                sku = it.sku,
                                price = it.price.toString(),
                                quantity = it.stockQuantity.toString()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // If offline/error, use data passed from search result
                val singleImage = product.product.imageUrl
                if (singleImage.isNotBlank()) _existingImageUrls.value = listOf(singleImage)

                if (product.variants.isNotEmpty()) {
                    _variants.value = product.variants
                }
            }
            
        }
    }

    // ============================================================================================
    // 3. SAVE / UPDATE / DELETE LOGIC
    // ============================================================================================

    fun saveProduct() {
        viewModelScope.launch {
            _saveState.value = ProductState.Loading

            // 1. Basic Validation
            if (productName.value.isBlank() || category.value.isBlank() || gender.value.isBlank() || _variants.value.isEmpty()) {
                _saveState.value = ProductState.Error("Please fill all required fields")
                return@launch
            }

            // 2. Color Validation
            if (_variants.value.any { v -> validColors.none { it.equals(v.colour, true) } }) {
                _saveState.value = ProductState.Error("One or more variants have an invalid color.")
                return@launch
            }

            // 3. Price Validation
            if (_variants.value.any { (it.price.toDoubleOrNull() ?: 0.0) <= 0.0 }) {
                _saveState.value = ProductState.Error("Price cannot be 0.")
                return@launch
            }

            // 4. Size Validation
            if (_variants.value.any { it.size.isBlank() }) {
                _saveState.value = ProductState.Error("Please select a size for all variants.")
                return@launch
            }

            // 5. Stock Validation
            if (_variants.value.any { (it.quantity.toIntOrNull() ?: -1) < 0 }) {
                _saveState.value = ProductState.Error("Stock cannot be negative.")
                return@launch
            }

            // 6. SKU Validation
            // Check Duplicates within the form
            val currentFormSkus = _variants.value.map { it.sku.trim().uppercase() }
            if (currentFormSkus.size != currentFormSkus.distinct().size) {
                _saveState.value = ProductState.Error("Duplicate SKUs found in the form.")
                return@launch
            }

            // B. Check against Database (Fresh Fetch to avoid stale data)
            try {
                val snapshot = firestore.collection("products").get().await()
                val dbSkus = mutableListOf<String>()
                for (doc in snapshot.documents) {
                    if (doc.id == currentProductId) continue // Skip self
                    val vars = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                    vars.forEach { v ->
                        val s = v["sku"] as? String
                        if (!s.isNullOrBlank()) dbSkus.add(s.trim().uppercase()) // Add Trimmed
                    }
                }

                // Update local state for UI red indicators
                _takenSkus.value = dbSkus

                // If any form SKU exists in DB
                if (currentFormSkus.any { it in dbSkus }) {
                    _saveState.value = ProductState.Error("One or more SKUs already exist in database!")
                    return@launch
                }

                // Proceed Save...
                val productId = currentProductId ?: UUID.randomUUID().toString()

                val finalImageUrls = _existingImageUrls.value.toMutableList()
                for ((index, uri) in _selectedImages.value.withIndex()) {
                    val unique = "${productId}_${System.currentTimeMillis()}_$index.jpg"
                    val ref = storage.reference.child("product_images/$unique")
                    ref.putFile(uri).await()
                    finalImageUrls.add(ref.downloadUrl.await().toString())
                }
                val cover = finalImageUrls.firstOrNull() ?: ""

                val productEntity = ProductEntity(productId, productName.value, productDesc.value, category.value, gender.value, cover)
                val imageEntities = finalImageUrls.map { ProductImageEntity(productId = productId, imageUrl = it) }
                val variantEntities = _variants.value.map { ui ->
                    ProductVariantEntity(0, productId, ui.size, ui.colour,
                        ui.sku.trim().uppercase(),
                        ui.price.toDoubleOrNull() ?: 0.0,
                        ui.quantity.toIntOrNull() ?: 0)
                }

                productDao.insertProduct(productEntity)
                productDao.insertImages(imageEntities)
                productDao.insertVariants(variantEntities)

                val productData = mapOf(
                    "productId" to productId, "name" to productName.value, "description" to productDesc.value,
                    "category" to category.value, "gender" to gender.value, "imageUrl" to cover,
                    "images" to finalImageUrls,
                    "variants" to variantEntities.map { mapOf("sku" to it.sku, "size" to it.size, "colour" to it.colour, "qty" to it.stockQuantity, "price" to it.price) }
                )
                firestore.collection("products").document(productId).set(productData).await()

                _saveState.value = ProductState.Success
                loadProducts()

            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Error saving")
            }
        }
    }

    fun deleteProduct() {
        val id = currentProductId ?: return
        viewModelScope.launch {
            _saveState.value = ProductState.Loading
            try {
                // Delete from Room
                productDao.deleteProduct(id)
                // Delete from Firestore
                firestore.collection("products").document(id).delete().await()

                _saveState.value = ProductState.Success
                loadProducts() // Refresh list
            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Delete failed")
            }
        }
    }

    // ============================================================================================
    // 4. HELPER FUNCTIONS
    // ============================================================================================

    // --- Image Helpers ---
    fun onImagesSelected(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value + uris
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun removeExistingImage(url: String) {
        _existingImageUrls.value = _existingImageUrls.value - url
    }

    // --- Variant Helpers ---
    fun addVariantCard() {
        val current = _variants.value.toMutableList()
        current.add(VariantUiState())
        _variants.value = current
    }

    fun removeVariantCard(id: String) {
        if(_variants.value.size > 1) {
            _variants.value = _variants.value.filter { it.id != id }
        }
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

    // Smart Size Blocking: Only block size if color matches
    fun getUnavailableSizes(currentId: String, currentColor: String): List<String> {
        return _variants.value
            .filter {
                it.id != currentId &&
                        it.colour.equals(currentColor, ignoreCase = true) &&
                        it.size.isNotBlank()
            }
            .map { it.size }
    }

    // --- Reset Form ---
    fun resetState() {
        currentProductId = null
        _saveState.value = ProductState.Idle
        productName.value = ""
        productDesc.value = ""
        category.value = ""
        gender.value = ""
        _selectedImages.value = emptyList()
        _existingImageUrls.value = emptyList()
        _variants.value = listOf(VariantUiState())
    }

    fun fetchTakenSkus() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("products").get().await()
                val skus = mutableListOf<String>()

                for (doc in snapshot.documents) {
                    // Skip the current product so we don't flag its own SKUs as "Taken" during Edit
                    if (doc.id == currentProductId) continue

                    val variants = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                    variants.forEach { v ->
                        val sku = v["sku"] as? String
                        if (!sku.isNullOrBlank()) skus.add(sku)
                    }
                }
                _takenSkus.value = skus
            } catch (e: Exception) {
                // Ignore error, validation might just be weaker offline
            }
        }
    }
}