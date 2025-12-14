package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT SUM(grandTotal) FROM pos_orders WHERE orderDate BETWEEN :startDate AND :endDate")
    suspend fun getSalesInRange(startDate: Date, endDate: Date): Double?

    @Query("""
        SELECT SUM(i.quantity) 
        FROM pos_order_items i 
        INNER JOIN pos_orders o ON i.posOrderId = o.id 
        WHERE o.orderDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getItemsSoldInRange(startDate: Date, endDate: Date): Int?

    @Query("SELECT COUNT(*) FROM pos_orders WHERE orderDate BETWEEN :startDate AND :endDate")
    suspend fun getOrderCountInRange(startDate: Date, endDate: Date): Int

    @Transaction
    suspend fun syncPOSData(orders: List<POSOrderEntity>, items: List<POSOrderItemEntity>) {
        // 1. Insert/Update Orders
        if (orders.isNotEmpty()) {
            insertOrders(orders)

            // 2. Clear existing items for these orders to prevent duplicates
            val orderIds = orders.map { it.id }
            deleteItemsForOrders(orderIds)
        }

        // 3. Insert fresh items from Firestore
        if (items.isNotEmpty()) {
            insertOrderItems(items)
        }
    }
}