package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.miniproject.data.entity.CustomerEntity

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers WHERE email = :email")
    suspend fun login(email: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE customerId = :id")
    suspend fun getCustomerById(id: String): CustomerEntity

    @Query("SELECT EXISTS(SELECT 1 FROM customers WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1")
    suspend fun getCustomerByEmail(email: String): CustomerEntity

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
}
