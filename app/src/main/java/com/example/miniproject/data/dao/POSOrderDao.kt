package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.miniproject.data.entity.BestSellerItem
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity
import java.util.Date

@Dao
interface POSOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: POSOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<POSOrderItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<POSOrderEntity>)

    @Query("SELECT * FROM pos_orders ORDER BY orderDate DESC")
    suspend fun getAllPOSOrders(): List<POSOrderEntity>

    @Query("SELECT * FROM pos_order_items WHERE posOrderId = :orderId")
    suspend fun getPOSOrderItems(orderId: Long): List<POSOrderItemEntity>

    @Query("DELETE FROM pos_order_items WHERE posOrderId IN (:orderIds)")
    suspend fun deleteItemsForOrders(orderIds: List<Long>)

    @Transaction
    suspend fun syncPOSData(orders: List<POSOrderEntity>, items: List<POSOrderItemEntity>) {
        if (orders.isNotEmpty()) {
            insertOrders(orders)
            val orderIds = orders.map { it.id }
            deleteItemsForOrders(orderIds)
        }
        if (items.isNotEmpty()) {
            insertOrderItems(items)
        }
    }

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM pos_orders WHERE orderDate BETWEEN :start AND :end")
    suspend fun getRevenueInRange(start: Date, end: Date): Double

    @Query("SELECT COUNT(*) FROM pos_orders WHERE orderDate BETWEEN :start AND :end")
    suspend fun getOrderCountInRange(start: Date, end: Date): Int

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM pos_order_items i
        JOIN pos_orders o ON i.posOrderId = o.id
        WHERE o.orderDate BETWEEN :start AND :end
    """)
    suspend fun getItemsSoldInRange(start: Date, end: Date): Int

    @Query("""
        SELECT i.productName as name, p.imageUrl, SUM(i.quantity) as totalQty, SUM(i.price * i.quantity) as totalPrice
        FROM pos_order_items i
        JOIN pos_orders o ON i.posOrderId = o.id
        LEFT JOIN products p ON i.productId = p.productId
        WHERE o.orderDate BETWEEN :start AND :end
        GROUP BY i.productId
        ORDER BY totalQty DESC
        LIMIT 3
    """)
    suspend fun getBestSellersInRange(start: Date, end: Date): List<BestSellerItem>

    @Query("DELETE FROM pos_order_items WHERE posOrderId = :orderId")
    suspend fun deleteItemsByOrderId(orderId: Long)

    @Query("SELECT * FROM pos_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): POSOrderEntity?

    @Query("SELECT * FROM pos_orders WHERE orderDate BETWEEN :start AND :end ORDER BY orderDate DESC")
    suspend fun getOrdersInRange(start: Date, end: Date): List<POSOrderEntity>
}