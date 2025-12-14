package com.example.miniproject.repository

import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.dao.OrderDao
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun placeOrder(order: OrderEntity, items: List<OrderItemEntity>): Result<Long> {
        return try {
            // 1. Save to Local DB (Room)
            val newOrderId = orderDao.insertOrder(order)
            val itemsWithId = items.map { it.copy(orderId = newOrderId) }
            orderDao.insertOrderItem(itemsWithId)

            // 2. Clear Cart
            cartDao.deleteAllCartItems()

            // 3. Save to Firestore (Cloud) for backup/admin
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
                // We don't await() here strictly to allow offline success,
                // but for this example we will to ensure cloud sync.
                .await()

            Result.success(newOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderById(id: Long): OrderEntity {
        return orderDao.getOrderById(id)
    }

    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity> {
        return orderDao.getOrderItems(orderId)
    }
}