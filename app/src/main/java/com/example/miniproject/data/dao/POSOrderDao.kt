package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity

@Dao
interface POSOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: POSOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<POSOrderItemEntity>)

    @Query("SELECT * FROM pos_orders ORDER BY orderDate DESC")
    suspend fun getAllPOSOrders(): List<POSOrderEntity>

    @Query("SELECT * FROM pos_order_items WHERE posOrderId = :orderId")
    suspend fun getPOSOrderItems(orderId: Long): List<POSOrderItemEntity>
}