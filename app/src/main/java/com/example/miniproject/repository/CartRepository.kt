package com.example.miniproject.repository

import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.entity.CartEntity
import kotlinx.coroutines.flow.Flow

class CartRepository (private val cartDao: CartDao){
    val allCartItems: Flow<List<CartEntity>> = cartDao.getAllCartItems()

    suspend fun addCartItem(cartItem: CartEntity) {
        val existingCartItem = cartDao.getCartItemByVariantSku(cartItem.variantSku)
        if (existingCartItem != null) {
            val updatedCartItem =
                existingCartItem.copy(quantity = existingCartItem.quantity + cartItem.quantity)
                cartDao.updateCartItem(updatedCartItem)
        }else{
            cartDao.insertCartItem(cartItem)
        }
    }
    suspend fun updateQuantity(cartItem: CartEntity, newQuantity: Int) {
        if (newQuantity > 0) {
            cartDao.updateCartItem(cartItem.copy(quantity = newQuantity))
        } else {
            cartDao.deleteCartItem(cartItem)
        }
    }
    suspend fun removeCartItem(cartItem: CartEntity) {
        cartDao.deleteCartItem(cartItem)
    }
    suspend fun clearCart() {
        cartDao.deleteAllCartItems()
    }
}