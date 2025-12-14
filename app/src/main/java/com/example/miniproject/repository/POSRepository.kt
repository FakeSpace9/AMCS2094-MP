package com.example.miniproject.repository

import com.example.miniproject.data.dao.POSOrderDao
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class POSRepository(
    private val posOrderDao: POSOrderDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun placePOSOrder(order: POSOrderEntity, items: List<POSOrderItemEntity>): Result<Long> {
        return try {
            // 1. Save to Local Room Database (Separate Table)
            val newOrderId = posOrderDao.insertOrder(order)
            val itemsWithId = items.map { it.copy(posOrderId = newOrderId) }
            posOrderDao.insertOrderItems(itemsWithId)

            // 2. Save to Firestore (Separate Collection: "pos_orders")
            val orderData = mapOf(
                "posOrderId" to newOrderId,
                "cashierId" to order.cashierId,
                "customerEmail" to (order.customerEmail ?: "Walk-in"),
                "date" to order.orderDate,
                "grandTotal" to order.grandTotal,
                "paymentMethod" to order.paymentMethod,
                "items" to itemsWithId.map { item ->
                    mapOf(
                        "sku" to item.variantSku,
                        "name" to item.productName,
                        "qty" to item.quantity,
                        "price" to item.price
                    )
                }
            )

            firestore.collection("pos_orders") // Separate collection
                .document(newOrderId.toString())
                .set(orderData)
                .await()

            Result.success(newOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}