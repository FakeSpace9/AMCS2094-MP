package com.example.miniproject.data.dao

import androidx.room.*
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductVariantEntity

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment WHERE customerId = :customerId")
    suspend fun getPayments(customerId: String): List<PaymentEntity>

    @Query("SELECT * FROM payment WHERE paymentId = :id")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payment WHERE paymentId = :id")
    suspend fun deletePayment(id: Long)

    @Query("UPDATE payment SET isDefault = 0 WHERE customerId = :customerId")
    suspend fun clearDefault(customerId: String)

    @Query("UPDATE payment SET isDefault = 1 WHERE paymentId = :id")
    suspend fun setDefault(id: Long)


}
