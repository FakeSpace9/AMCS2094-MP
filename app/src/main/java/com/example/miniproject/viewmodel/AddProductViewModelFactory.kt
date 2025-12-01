package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.ProductDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddProductViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val productDao: ProductDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductFormViewModel(firestore, storage, productDao) as T
        }
        if (modelClass.isAssignableFrom(ProductSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductSearchViewModel(firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}