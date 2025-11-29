package com.example.miniproject.repository

import com.example.miniproject.data.entity.Product
import com.example.miniproject.data.entity.ProductVariant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")
    // A separate collection to store the running number
    private val counterDocRef = firestore.collection("metadata").document("product_counter")

    suspend fun saveProductWithVariants(
        // Note: We don't pass the ID anymore, we generate it here!
        productData: Product,
        variants: List<ProductVariant>
    ): Result<Boolean> {
        return try {
            firestore.runTransaction { transaction ->

                // 1. READ THE CURRENT COUNTER
                val snapshot = transaction.get(counterDocRef)

                // If it doesn't exist (first run), start at 0. Otherwise get value.
                val currentCount = if (snapshot.exists()) {
                    snapshot.getLong("count") ?: 0
                } else {
                    0
                }

                // 2. CALCULATE NEW ID
                val newCount = currentCount + 1
                // Format: PROD-001, PROD-002, etc. (%03d means 3 digits with leading zeros)
                val newId = String.format("PROD-%03d", newCount)

                // 3. PREPARE THE PRODUCT WITH NEW ID
                val finalProduct = productData.copy(productId = newId)

                // 4. WRITE: Save the Product
                val productRef = productsCollection.document(newId)
                transaction.set(productRef, finalProduct)

                // 5. WRITE: Save the Variants
                // We need to loop through variants and update their FK (productId)
                val variantsCollection = productRef.collection("variants")

                variants.forEach { variant ->
                    // Set the Foreign Key to the new ID we just generated
                    val finalVariant = variant.copy(productId = newId)

                    val variantRef = variantsCollection.document(finalVariant.productVariantId)
                    transaction.set(variantRef, finalVariant)
                }

                // 6. UPDATE THE COUNTER
                // Save the new number back to the metadata collection
                if (snapshot.exists()) {
                    transaction.update(counterDocRef, "count", newCount)
                } else {
                    transaction.set(counterDocRef, mapOf("count" to newCount))
                }

                // Return true if successful
                true
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}