package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.repository.ProductRepository // Import Repo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddProductViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val productDao: ProductDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Form VM (Entry/Edit)
        if (modelClass.isAssignableFrom(ProductFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductFormViewModel(firestore, storage, productDao) as T
        }

        // Search VM (Search/List)
        if (modelClass.isAssignableFrom(ProductSearchViewModel::class.java)) {
            // Initialize Repository here
            val repository = ProductRepository(firestore, productDao)

            @Suppress("UNCHECKED_CAST")
            return ProductSearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}