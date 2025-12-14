package com.example.miniproject.data.dao

import androidx.room.*
import com.example.miniproject.data.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions ORDER BY startDate DESC")
    fun getAllPromotions(): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions WHERE code = :code LIMIT 1")
    suspend fun getPromotionByCode(code: String): PromotionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity)

    @Delete
    suspend fun deletePromotion(promotion: PromotionEntity)

    @Query("DELETE FROM promotions WHERE promotionId = :id")
    suspend fun deletePromotionById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(promotions: List<PromotionEntity>)
}