package com.example.miniproject.repository

import com.example.miniproject.data.dao.POSOrderDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class POSRepository(
    private val posOrderDao: POSOrderDao,
    private val productDao: ProductDao, // Add ProductDao
    private val firestore: FirebaseFirestore
) {

    suspend fun placePOSOrder(order: POSOrderEntity, items: List<POSOrderItemEntity>): Result<Long> {
        return try {
            // 1. Save to Local Room Database
            val newOrderId = posOrderDao.insertOrder(order)
            val itemsWithId = items.map { it.copy(posOrderId = newOrderId) }
            posOrderDao.insertOrderItems(itemsWithId)

            // 2. Decrease Stock (Local & Cloud)
            items.forEach { item ->
                // Local Room
                productDao.decreaseStock(item.variantSku, item.quantity)

                // Firestore
                updateFirestoreStock(item.productId, item.variantSku, item.quantity)
            }

            // 3. Save to Firestore POS Collection
            val orderData = mapOf(
                "posOrderId" to newOrderId,
                "cashierId" to order.cashierId,
                "customerEmail" to (order.customerEmail ?: "Walk-in"),
                "date" to order.orderDate,
                "subTotal" to order.totalAmount,
                "discount" to order.discount,
                "grandTotal" to order.grandTotal,
                "grandTotal" to order.grandTotal,
                "paymentMethod" to order.paymentMethod,
                "items" to itemsWithId.map { item ->
                    mapOf(
                        "productId" to item.productId,
                        "sku" to item.variantSku,
                        "name" to item.productName,
                        "size" to item.size,
                        "color" to item.color,
                        "qty" to item.quantity,
                        "price" to item.price
                    )
                }
            )

            firestore.collection("pos_orders")
                .document(newOrderId.toString())
                .set(orderData)
                .await()

            Result.success(newOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Same helper function for Firestore
    private suspend fun updateFirestoreStock(productId: String, sku: String, qtySold: Int) {
        try {
            val productRef = firestore.collection("products").document(productId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                val variants = snapshot.get("variants") as? List<Map<String, Any>> ?: emptyList()

                val updatedVariants = variants.map { variant ->
                    if (variant["sku"] == sku) {
                        val currentQty = (variant["qty"] as? Number)?.toInt() ?: 0
                        val newQty = (currentQty - qtySold).coerceAtLeast(0)
                        variant.toMutableMap().apply { put("qty", newQty) }
                    } else {
                        variant
                    }
                }
                transaction.update(productRef, "variants", updatedVariants)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTodayRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time

        return Pair(start, end)
    }

    suspend fun getTodaySales(): Double {
        val (start, end) = getTodayRange()
        return posOrderDao.getRevenueInRange(start, end)
    }


    suspend fun getTodayOrderCount(): Int {
        val (start, end) = getTodayRange()
        return posOrderDao.getOrderCountInRange(start, end)
    }

    suspend fun syncPOSOrders() {
        try {
            val snapshot = firestore.collection("pos_orders").get().await()

            val orders = mutableListOf<POSOrderEntity>()
            val allItems = mutableListOf<POSOrderItemEntity>()

            for (doc in snapshot.documents) {
                // 1. Parse Order Fields
                // Firestore document ID is the string version of the Room Long ID
                val orderId = doc.id.toLongOrNull() ?: continue

                // Safe parsing using explicit casts
                val cashierId = doc.getString("cashierId") ?: ""
                val email = doc.getString("customerEmail")

                // Handle Timestamp -> Date conversion
                val timestamp = doc.getTimestamp("date")
                val orderDate = timestamp?.toDate() ?: java.util.Date()

                val subTotal = doc.getDouble("subTotal") ?: 0.0
                val discount = doc.getDouble("discount") ?: 0.0
                val grandTotal = doc.getDouble("grandTotal") ?: 0.0
                val paymentMethod = doc.getString("paymentMethod") ?: "Unknown"

                val order = POSOrderEntity(
                    id = orderId,
                    cashierId = cashierId,
                    customerEmail = email,
                    orderDate = orderDate,
                    totalAmount = subTotal,
                    discount = discount,
                    grandTotal = grandTotal,
                    paymentMethod = paymentMethod,
                )
                orders.add(order)

                // 2. Parse Items Array
                val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                itemsList.forEach { itemMap ->
                    allItems.add(
                        POSOrderItemEntity(
                            id = 0,
                            posOrderId = orderId,
                            productId = itemMap["productId"] as? String ?: "", // Read ID
                            productName = itemMap["name"] as? String ?: "",
                            variantSku = itemMap["sku"] as? String ?: "",
                            size = itemMap["size"] as? String ?: "",           // Read Size
                            color = itemMap["color"] as? String ?: "",         // Read Color
                            price = (itemMap["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (itemMap["qty"] as? Long)?.toInt() ?: 0
                        )
                    )
                }
            }

            // 3. Save to Room
            posOrderDao.syncPOSData(orders, allItems)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}