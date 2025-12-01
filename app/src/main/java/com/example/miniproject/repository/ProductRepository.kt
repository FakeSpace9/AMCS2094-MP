package com.example.miniproject.repository

import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductImageEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import com.example.miniproject.viewmodel.VariantUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(
    private val firestore: FirebaseFirestore,
    private val productDao: ProductDao
) {

    // 1. Get Data from Room (Offline Fast)
    suspend fun searchProductsLocal(query: String): List<ProductSearchResult> {
        val results = productDao.searchProducts(query)

        // Populate the 'variants' list for the UI (Room ignores this field by default)
        return results.map { result ->
            val dbVariants = productDao.getVariantsForProduct(result.product.productId)
            val uiVariants = dbVariants.map {
                VariantUiState(
                    id = UUID.randomUUID().toString(), // UI needs ID for keys
                    size = it.size,
                    colour = it.colour,
                    sku = it.sku,
                    price = it.price.toString(),
                    quantity = it.stockQuantity.toString()
                )
            }
            result.apply { this.variants = uiVariants }
        }
    }

    // 2. Sync Firestore to Room (Background)
    suspend fun syncProducts() {
        try {
            val snapshot = firestore.collection("products").get().await()

            val products = mutableListOf<ProductEntity>()
            val allVariants = mutableListOf<ProductVariantEntity>()
            val allImages = mutableListOf<ProductImageEntity>()

            for (doc in snapshot.documents) {
                val id = doc.getString("productId") ?: continue
                val name = doc.getString("name") ?: ""
                val desc = doc.getString("description") ?: ""
                val cat = doc.getString("category") ?: ""
                val gend = doc.getString("gender") ?: ""
                val img = doc.getString("imageUrl") ?: ""

                products.add(ProductEntity(id, name, desc, cat, gend, img))

                // Parse Variants
                val vars = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                vars.forEach { v ->
                    allVariants.add(
                        ProductVariantEntity(
                            0, // Auto-generate ID
                            id,
                            v["size"] as? String ?: "",
                            v["colour"] as? String ?: "",
                            v["sku"] as? String ?: "",
                            (v["price"] as? Number)?.toDouble() ?: 0.0,
                            (v["qty"] as? Long)?.toInt() ?: 0
                        )
                    )
                }

                // Parse Images
                val imgs = doc.get("images") as? List<String> ?: emptyList()
                imgs.forEach { url ->
                    allImages.add(ProductImageEntity(0, id, url))
                }
            }

            // Save to Room using Transaction
            productDao.syncData(products, allVariants, allImages)

        } catch (e: Exception) {
            e.printStackTrace()
            // If sync fails, we just continue using local data
        }
    }
}