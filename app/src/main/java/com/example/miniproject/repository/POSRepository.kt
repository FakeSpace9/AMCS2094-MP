package com.example.miniproject.repository

import com.example.miniproject.data.dao.POSOrderDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import com.example.miniproject.data.entity.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class POSRepository(
    private val posOrderDao: POSOrderDao,
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore
) {

    // --- NEW METHOD ---
    suspend fun getProductById(productId: String): ProductEntity? {
        return productDao.getProductById(productId)
    }
    // ------------------

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
                val orderId = doc.id.toLongOrNull() ?: continue
                val cashierId = doc.getString("cashierId") ?: ""
                val email = doc.getString("customerEmail")
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

                val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                itemsList.forEach { itemMap ->
                    allItems.add(
                        POSOrderItemEntity(
                            id = 0,
                            posOrderId = orderId,
                            productId = itemMap["productId"] as? String ?: "",
                            productName = itemMap["name"] as? String ?: "",
                            variantSku = itemMap["sku"] as? String ?: "",
                            size = itemMap["size"] as? String ?: "",
                            color = itemMap["color"] as? String ?: "",
                            price = (itemMap["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (itemMap["qty"] as? Long)?.toInt() ?: 0
                        )
                    )
                }
            }
            posOrderDao.syncPOSData(orders, allItems)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllPOSOrders(): List<POSOrderEntity> {
        return posOrderDao.getAllPOSOrders()
    }

    suspend fun getPOSOrderItems(orderId: Long): List<POSOrderItemEntity> {
        return posOrderDao.getPOSOrderItems(orderId)
    }

    suspend fun updatePOSOrderWithItems(
        order: POSOrderEntity,
        newItems: List<POSOrderItemEntity>,
        oldItems: List<POSOrderItemEntity>
    ): Result<Boolean> {
        return try {
            val db = firestore
            db.runTransaction { transaction ->
                val stockAdjustments = mutableMapOf<String, Int>()

                newItems.forEach { newItem ->
                    val oldItem = oldItems.find { it.variantSku == newItem.variantSku }
                    val oldQty = oldItem?.quantity ?: 0
                    val diff = newItem.quantity - oldQty
                    if (diff != 0) stockAdjustments[newItem.variantSku] = diff
                }

                oldItems.forEach { oldItem ->
                    if (newItems.none { it.variantSku == oldItem.variantSku }) {
                        stockAdjustments[oldItem.variantSku] = -oldItem.quantity
                    }
                }

                stockAdjustments.forEach { (sku, qtyChange) ->
                    val productId = newItems.find { it.variantSku == sku }?.productId
                        ?: oldItems.find { it.variantSku == sku }?.productId

                    if (productId != null) {
                        val productRef = db.collection("products").document(productId)
                        val snapshot = transaction.get(productRef)

                        if (snapshot.exists()) {
                            val variants = snapshot.get("variants") as? List<Map<String, Any>> ?: emptyList()
                            val updatedVariants = variants.map { variant ->
                                if (variant["sku"] == sku) {
                                    val currentStock = (variant["qty"] as? Number)?.toInt() ?: 0
                                    val newStock = (currentStock - qtyChange).coerceAtLeast(0)
                                    variant.toMutableMap().apply { put("qty", newStock) }
                                } else {
                                    variant
                                }
                            }
                            transaction.update(productRef, "variants", updatedVariants)
                        }
                    }
                }

                val orderRef = db.collection("pos_orders").document(order.id.toString())
                val firestoreItems = newItems.map { item ->
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

                transaction.update(orderRef, mapOf(
                    "items" to firestoreItems,
                    "subTotal" to order.totalAmount,
                    "grandTotal" to order.grandTotal,
                    "discount" to order.discount,
                    "customerEmail" to (order.customerEmail ?: ""),
                    "paymentMethod" to order.paymentMethod,
                    "status" to order.status
                ))
            }.await()

            posOrderDao.insertOrder(order)
            posOrderDao.deleteItemsByOrderId(order.id)
            posOrderDao.insertOrderItems(newItems)

            newItems.forEach { newItem ->
                val oldItem = oldItems.find { it.variantSku == newItem.variantSku }
                val oldQty = oldItem?.quantity ?: 0
                val diff = newItem.quantity - oldQty
                if (diff != 0) {
                    productDao.decreaseStock(newItem.variantSku, diff)
                }
            }
            oldItems.forEach { oldItem ->
                if (newItems.none { it.variantSku == oldItem.variantSku }) {
                    productDao.decreaseStock(oldItem.variantSku, -oldItem.quantity)
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getReceiptItems(items: List<POSOrderItemEntity>): List<ReceiptItem> {
        return items.map { item ->
            val product = productDao.getProductById(item.productId)
            ReceiptItem(
                name = item.productName,
                variant = "${item.size} / ${item.color}",
                quantity = item.quantity,
                unitPrice = item.price,
                totalPrice = item.price * item.quantity,
                imageUrl = product?.imageUrl ?: ""
            )
        }
    }

    private fun getDateRange(date: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.time

        return Pair(start, end)
    }

    suspend fun getOrdersForDate(date: Date): List<POSOrderEntity> {
        val (start, end) = getDateRange(date)
        return posOrderDao.getOrdersInRange(start, end)
    }

    suspend fun getPOSOrderById(id: Long): POSOrderEntity? {
        return posOrderDao.getOrderById(id)
    }
}