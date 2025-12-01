package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductImageEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import com.example.miniproject.viewmodel.VariantUiState // Import this

// Updated Data Class with 'variants' list
data class ProductSearchResult(
    @Embedded val product: ProductEntity,
    val totalStock: Int?,
    val minPrice: Double?,
    val maxPrice: Double?,
    val displaySku: String?,
){
    @Ignore
    var variants: List<VariantUiState> = emptyList()
}

@Dao
interface ProductDao {
    // ... (Your existing DAO methods remain the same) ...
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ProductImageEntity>)

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    suspend fun getVariantsForProduct(productId: String): List<ProductVariantEntity>

    @Query("SELECT * FROM product_images WHERE productId = :productId")
    suspend fun getImagesForProduct(productId: String): List<ProductImageEntity>

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)

    // Note: This Room query doesn't populate the new 'variants' list, which is fine
    // because we are using Firestore for the search now.
    @Query("""
        SELECT p.*, 
               SUM(v.stockQuantity) as totalStock, 
               MIN(v.price) as minPrice, 
               MAX(v.price) as maxPrice,  -- <--- ADDED THIS
               MAX(v.sku) as displaySku
        FROM products p 
        LEFT JOIN product_variants v ON p.productId = v.productId 
        WHERE p.name LIKE '%' || :query || '%' OR p.category LIKE '%' || :query || '%'
        GROUP BY p.productId
        ORDER BY p.name ASC
    """)
    suspend fun searchProducts(query: String): List<ProductSearchResult>
}