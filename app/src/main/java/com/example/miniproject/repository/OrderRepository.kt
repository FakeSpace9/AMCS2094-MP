package com.example.miniproject.repository

import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.dao.OrderDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore
) {

    // ... (Keep placeOrder, getOrderById, getOrderItems as they are) ...
    suspend fun placeOrder(order: OrderEntity, items: List<OrderItemEntity>): Result<Long> {
        // (Your existing placeOrder code here...)
        return try {
            val newOrderId = orderDao.insertOrder(order)
            val itemsWithId = items.map { it.copy(orderId = newOrderId) }
            orderDao.insertOrderItem(itemsWithId)
            cartDao.deleteAllCartItems()
            items.forEach { item ->
                productDao.decreaseStock(item.variantSku, item.quantity)
                updateFirestoreStock(item.productId, item.variantSku, item.quantity)
            }

            // Save to Firestore
            val orderData = mapOf(
                "orderId" to newOrderId,
                "customerId" to order.customerId,
                "date" to order.orderDate,
                "total" to order.grandTotal,
                "status" to order.status,
                "payment" to order.paymentMethod,
                "address" to order.deliveryAddress,
                "items" to itemsWithId.map { item ->
                    mapOf(
                        "sku" to item.variantSku,
                        "name" to item.productName,
                        "qty" to item.quantity,
                        "price" to item.price,
                        "productId" to item.productId,
                        "imageUrl" to item.imageUrl,
                        "size" to item.size,
                        "color" to item.color
                    )
                }
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
                    } else { variant }
                }
                transaction.update(productRef, "variants", updatedVariants)
            }.await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun getOrderById(id: Long): OrderEntity = orderDao.getOrderById(id)
    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity> = orderDao.getOrderItems(orderId)

    // --- NEW SYNC FUNCTION ---
    suspend fun syncOrders() {
        try {
            val snapshot = firestore.collection("orders").get().await()

            val orders = mutableListOf<OrderEntity>()
            val allItems = mutableListOf<OrderItemEntity>()

            for (doc in snapshot.documents) {
                // Parse ID
                val orderIdStr = doc.id
                val orderId = orderIdStr.toLongOrNull() ?: continue

                // Parse Order
                orders.add(OrderEntity(
                    id = orderId.toInt(), // Room ID
                    customerId = doc.getString("customerId") ?: "",
                    orderDate = doc.getLong("date") ?: System.currentTimeMillis(),
                    totalAmount = doc.getDouble("total") ?: 0.0,
                    status = doc.getString("status") ?: "Completed",
                    shippingFee = 0.0,
                    grandTotal = doc.getDouble("total") ?: 0.0,
                    discount = 0.0,
                    paymentMethod = doc.getString("payment") ?: "",
                    deliveryAddress = doc.getString("address") ?: ""
                ))

                // Parse Items
                val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                itemsList.forEach { map ->
                    allItems.add(OrderItemEntity(
                        orderId = orderId,
                        productId = map["productId"] as? String ?: "",
                        productName = map["name"] as? String ?: "",
                        variantSku = map["sku"] as? String ?: "",
                        size = map["size"] as? String ?: "",
                        color = map["color"] as? String ?: "",
                        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (map["qty"] as? Long)?.toInt() ?: 0,
                        imageUrl = map["imageUrl"] as? String ?: ""
                    ))
                }
            }

            // Save all to Room
            orderDao.syncOrders(orders, allItems)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getOrdersByCustomer(customerId: String): List<OrderEntity> {
        return orderDao.getOrdersByCustomer(customerId)
    }

    fun getOrdersByStatusFlow(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            // 1. Update Local Room DB
            orderDao.updateOrderStatus(orderId, newStatus)

            // 2. Update Firestore so the user sees the change
            firestore.collection("orders") // Check your Firestore collection name
                .document(orderId)
                .update("status", newStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}