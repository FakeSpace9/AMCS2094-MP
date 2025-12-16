package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.miniproject.data.entity.AddressEntity

@Dao
interface AddressDao {

    @Query("SELECT * FROM addresses WHERE customerId = :customerId")
    suspend fun getAddressesByCustomerId(customerId: String): List<AddressEntity>

    @Query("SELECT * FROM addresses WHERE addressId = :addressId")
    suspend fun getAddressById(addressId: Long): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE addressId = :id")
    suspend fun deleteAddressById(id: Long)

    @Query("DELETE FROM addresses WHERE customerId = :customerId")
    suspend fun deleteAddressByCustomerId(customerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAddress(addresses: List<AddressEntity>)


}
