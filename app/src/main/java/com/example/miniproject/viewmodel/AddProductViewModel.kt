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
    var gender = MutableStateFlow("Unisex")

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

    // Static Data
    val allSizes = listOf("XS", "S", "M", "L", "XL", "XXL")

    // ============================================================================================
    // 1. SEARCH & LOAD LOGIC (From Firestore)
    // ============================================================================================

    fun loadProducts() {
        viewModelScope.launch {
            try {
                // A. Fetch all products from Firestore
                val snapshot = firestore.collection("products").get().await()

                // B. Map Firestore Documents to our local ProductSearchResult model
                val allProducts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("productId") ?: return@mapNotNull null
                        val name = doc.getString("name") ?: ""
                        val desc = doc.getString("description") ?: ""
                        val cat = doc.getString("category") ?: ""
                        val gend = doc.getString("gender") ?: ""
                        val img = doc.getString("imageUrl") ?: ""

                        // Calculate Summary (Total Stock & Min Price) from variants array
                        val variantsList = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()

                        var totalStock = 0
                        var minPrice = Double.MAX_VALUE
                        var firstSku = ""

                        // Convert map to VariantUiState list for the "Details" view in Search Card
                        val parsedVariants = variantsList.mapIndexed { index, v ->
                            val qty = (v["qty"] as? Long)?.toInt() ?: 0
                            val price = (v["price"] as? Number)?.toDouble() ?: 0.0
                            val sku = (v["sku"] as? String) ?: ""
                            val size = (v["size"] as? String) ?: ""
                            val colour = (v["colour"] as? String) ?: ""

                            if (index == 0) firstSku = sku
                            totalStock += qty
                            if (price < minPrice) minPrice = price

                            // Return the object for the list
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

                        // FIXED: variants removed from constructor, added in apply block
                        ProductSearchResult(
                            product = ProductEntity(id, name, desc, cat, gend, img),
                            totalStock = totalStock,
                            minPrice = minPrice,
                            displaySku = firstSku
                        ).apply {
                            variants = parsedVariants // <--- Set manually here
                        }

                    } catch (e: Exception) {
                        null
                    }
                }

                // C. Filter & Sort Logic
                var filteredList = allProducts

                // 1. Filter by Search Query (Name, Category, or SKU)
                val query = searchQuery.value.lowercase().trim()
                if (query.isNotBlank()) {
                    filteredList = filteredList.filter {
                        it.product.name.lowercase().contains(query) ||
                                it.product.category.lowercase().contains(query) ||
                                (it.displaySku?.lowercase()?.contains(query) == true)
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
                    SortOption.NEWEST -> filteredList // You could add timestamp logic here
                    SortOption.NAME_A_Z -> filteredList.sortedBy { it.product.name }
                    SortOption.PRICE_LOW_HIGH -> filteredList.sortedBy { it.minPrice }
                    SortOption.PRICE_HIGH_LOW -> filteredList.sortedByDescending { it.minPrice }
                }

                _searchResults.value = filteredList

            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList() // Clear on error
            }
        }
    }

    fun getAvailableCategories(): List<String> {
        return listOf("All", "Tops", "Bottoms", "Outerwear", "Accessories", "Shoes")
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

            // Validation
            if (productName.value.isBlank() || _variants.value.isEmpty()) {
                _saveState.value = ProductState.Error("Please fill required fields (Name, Variants)")
                return@launch
            }

            try {
                // Generate ID if new, use existing if editing
                val productId = currentProductId ?: UUID.randomUUID().toString()

                // A. Handle Images (Merge Old List + Upload New Selected)
                val finalImageUrls = _existingImageUrls.value.toMutableList()

                for ((index, uri) in _selectedImages.value.withIndex()) {
                    val uniqueName = "${productId}_${System.currentTimeMillis()}_$index.jpg"
                    val storageRef = storage.reference.child("product_images/$uniqueName")

                    storageRef.putFile(uri).await()
                    finalImageUrls.add(storageRef.downloadUrl.await().toString())
                }

                // First image is the "Cover"
                val coverImage = finalImageUrls.firstOrNull() ?: ""

                // B. Prepare Database Entities (Room)
                val productEntity = ProductEntity(
                    productId = productId,
                    name = productName.value,
                    description = productDesc.value,
                    category = category.value,
                    gender = gender.value,
                    imageUrl = coverImage
                )

                val imageEntities = finalImageUrls.map { ProductImageEntity(productId = productId, imageUrl = it) }

                val variantEntities = _variants.value.map { ui ->
                    ProductVariantEntity(
                        productId = productId,
                        size = ui.size,
                        colour = ui.colour,
                        sku = ui.sku,
                        price = ui.price.toDoubleOrNull() ?: 0.0,
                        stockQuantity = ui.quantity.toIntOrNull() ?: 0
                    )
                }

                // C. Save to Room (Local Cache)
                productDao.insertProduct(productEntity)
                productDao.insertImages(imageEntities)
                productDao.insertVariants(variantEntities)

                // D. Save to Firestore (Cloud)
                val productData = mapOf(
                    "productId" to productId,
                    "name" to productName.value,
                    "description" to productDesc.value,
                    "category" to category.value,
                    "gender" to gender.value,
                    "imageUrl" to coverImage,
                    "images" to finalImageUrls, // Save List
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
                loadProducts() // Refresh the search list

            } catch (e: Exception) {
                _saveState.value = ProductState.Error(e.message ?: "Error saving product")
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
        _selectedImages.value = emptyList()
        _existingImageUrls.value = emptyList()
        _variants.value = listOf(VariantUiState())
    }
}