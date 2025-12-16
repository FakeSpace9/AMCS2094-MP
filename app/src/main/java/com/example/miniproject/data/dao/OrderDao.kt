package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.miniproject.data.entity.BestSellerItem
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: List<OrderItemEntity>)

    // --- NEW: Batch Operations for Sync ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("DELETE FROM orders")
    suspend fun clearOrders()

    @Query("DELETE FROM order_items")
    suspend fun clearOrderItems()

    @Transaction
    suspend fun syncOrders(orders: List<OrderEntity>, items: List<OrderItemEntity>) {
        // Optional: Clear old data to ensure we match Firebase exactly
        clearOrders()
        clearOrderItems()

        if (orders.isNotEmpty()) insertOrders(orders)
        if (items.isNotEmpty()) insertOrderItem(items)
    }
    // --------------------------------------

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity>

    @Query("""SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC""")
    suspend fun getOrdersByCustomer(customerId: String): List<OrderEntity>
    // --- ANALYTICS QUERIES (Keep existing) ---
    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM orders WHERE orderDate BETWEEN :start AND :end")
    suspend fun getRevenueInRange(start: Long, end: Long): Double

    @Query("SELECT COUNT(*) FROM orders WHERE orderDate BETWEEN :start AND :end")
    suspend fun getOrderCountInRange(start: Long, end: Long): Int

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM order_items i
        JOIN orders o ON i.orderId = o.id
        WHERE o.orderDate BETWEEN :start AND :end
    """)
    suspend fun getItemsSoldInRange(start: Long, end: Long): Int

    @Query("""
        SELECT i.productName as name, i.imageUrl, SUM(i.quantity) as totalQty, SUM(i.price * i.quantity) as totalPrice
        FROM order_items i
        JOIN orders o ON i.orderId = o.id
        WHERE o.orderDate BETWEEN :start AND :end
        GROUP BY i.productId
        ORDER BY totalQty DESC
        LIMIT 3
    """)
    suspend fun getBestSellersInRange(start: Long, end: Long): List<BestSellerItem>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
}