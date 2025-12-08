package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.miniproject.data.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM carts")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM carts WHERE id = :id")
    suspend fun deleteCartItemsById(id: Int)

    @Query("SELECT * FROM carts WHERE variantSku = :variantSku")
    suspend fun getCartItemByVariantSku(variantSku: String): CartEntity?

    @Query("DELETE FROM carts")
    suspend fun deleteAllCartItems()

}