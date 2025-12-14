package com.example.miniproject.repository

import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.dao.OrderDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val productDao: ProductDao, // Add ProductDao to constructor
    private val firestore: FirebaseFirestore
) {

    suspend fun placeOrder(order: OrderEntity, items: List<OrderItemEntity>): Result<Long> {
        return try {
            // 1. Save Order to Local DB
            val newOrderId = orderDao.insertOrder(order)
            val itemsWithId = items.map { it.copy(orderId = newOrderId) }
            orderDao.insertOrderItem(itemsWithId)

            // 2. Clear Cart
            cartDao.deleteAllCartItems()

            // 3. Decrease Stock (Local & Cloud)
            items.forEach { item ->
                // Local Room Update
                productDao.decreaseStock(item.variantSku, item.quantity)

                // Firestore Update
                updateFirestoreStock(item.productId, item.variantSku, item.quantity)
            }

            // 4. Save Order to Firestore
            val orderData = mapOf(
                "orderId" to newOrderId,
                "customerId" to order.customerId,
                "date" to order.orderDate,
                "total" to order.grandTotal,
                "items" to itemsWithId.map { item ->
                    mapOf(
                        "sku" to item.variantSku,
                        "name" to item.productName,
                        "qty" to item.quantity,
                        "price" to item.price
                    )
                },
                "address" to order.deliveryAddress,
                "payment" to order.paymentMethod
            )

            firestore.collection("orders")
                .document(newOrderId.toString())
                .set(orderData)
                .await()

            Result.success(newOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper to update stock in Firestore safely
    private suspend fun updateFirestoreStock(productId: String, sku: String, qtySold: Int) {
        try {
            val productRef = firestore.collection("products").document(productId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                // Get current variants list
                val variants = snapshot.get("variants") as? List<Map<String, Any>> ?: emptyList()

                // Create updated list
                val updatedVariants = variants.map { variant ->
                    if (variant["sku"] == sku) {
                        val currentQty = (variant["qty"] as? Number)?.toInt() ?: 0
                        val newQty = (currentQty - qtySold).coerceAtLeast(0)

                        // Return updated map for this variant
                        variant.toMutableMap().apply { put("qty", newQty) }
                    } else {
                        variant
                    }
                }

                // Write back to Firestore
                transaction.update(productRef, "variants", updatedVariants)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getOrderById(id: Long): OrderEntity {
        return orderDao.getOrderById(id)
    }

    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity> {
        return orderDao.getOrderItems(orderId)
    }
}