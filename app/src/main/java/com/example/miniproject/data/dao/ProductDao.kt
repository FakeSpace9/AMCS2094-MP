package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductImageEntity
import com.example.miniproject.data.entity.ProductVariantEntity
import com.example.miniproject.viewmodel.VariantUiState

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

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

    @Query("DELETE FROM products WHERE productId NOT IN (:activeProductIds)")
    suspend fun deleteProductsNotIn(activeProductIds: List<String>)

    @Query("DELETE FROM product_variants WHERE productId NOT IN (:activeProductIds)")
    suspend fun deleteVariantsNotIn(activeProductIds: List<String>)

    @Query("DELETE FROM product_images WHERE productId NOT IN (:activeProductIds)")
    suspend fun deleteImagesNotIn(activeProductIds: List<String>)

    @Query("SELECT * FROM products WHERE productId = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Transaction
    suspend fun syncData(products: List<ProductEntity>, variants: List<ProductVariantEntity>, images: List<ProductImageEntity>) {
        if (products.isNotEmpty()) insertProducts(products)
        if (variants.isNotEmpty()) insertVariants(variants)
        if (images.isNotEmpty()) insertImages(images)

        val activeIds = products.map { it.productId }
        if (activeIds.isNotEmpty()) {
            deleteProductsNotIn(activeIds)
            deleteVariantsNotIn(activeIds)
            deleteImagesNotIn(activeIds)
        }
    }
    @Transaction
    @Query("""
        SELECT p.*, 
               SUM(v.stockQuantity) as totalStock, 
               MIN(v.price) as minPrice, 
               MAX(v.price) as maxPrice,
               MAX(v.sku) as displaySku
        FROM products p 
        LEFT JOIN product_variants v ON p.productId = v.productId 
        WHERE p.name LIKE '%' || :query || '%' 
           OR p.category LIKE '%' || :query || '%'
           OR EXISTS (SELECT 1 FROM product_variants v2 WHERE v2.productId = p.productId AND v2.sku LIKE '%' || :query || '%')
        GROUP BY p.productId
        ORDER BY p.name ASC
    """)
    suspend fun searchProducts(query: String): List<ProductSearchResult>

    @Query("SELECT * FROM product_variants WHERE sku = :sku LIMIT 1")
    suspend fun getVariantBySku(sku: String): ProductVariantEntity?

    @Query("UPDATE product_variants SET stockQuantity = stockQuantity - :quantity WHERE sku = :sku")
    suspend fun decreaseStock(sku: String, quantity: Int)


}