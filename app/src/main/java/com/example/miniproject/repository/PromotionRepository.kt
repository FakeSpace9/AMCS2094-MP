package com.example.miniproject.repository

import com.example.miniproject.data.dao.PromotionDao
import com.example.miniproject.data.entity.PromotionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PromotionRepository(
    private val promotionDao: PromotionDao,
    private val firestore: FirebaseFirestore
) {
    val allPromotions: Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()

    suspend fun getPromotionByCode(code: String): PromotionEntity? {
        return promotionDao.getPromotionByCode(code)
    }

    suspend fun createPromotion(
        adminId: String,
        code: String,
        name: String,
        desc: String,
        rate: Double,
        start: Long,
        end: Long
    ): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()

            // 1. Create Entity for Local DB (Room)
            val promo = PromotionEntity(
                promotionId = id,
                adminId = adminId,
                code = code.uppercase(),
                name = name,
                description = desc,
                discountRate = rate,
                startDate = start,
                endDate = end,
                isPercentage = true
            )

            // 2. Save to Local Room DB
            promotionDao.insertPromotion(promo)

            // 3. Save to Firestore (Use a Map to ensure correct saving)
            val firestoreData = hashMapOf(
                "promotionId" to id,
                "adminId" to adminId,
                "code" to code.uppercase(),
                "name" to name,
                "description" to desc,
                "discountRate" to rate,
                "startDate" to start,
                "endDate" to end,
                "isPercentage" to true
            )

            firestore.collection("promotions")
                .document(id)
                .set(firestoreData) // Save the Map, not the Entity object
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace() // Log error to Logcat
            Result.failure(e)
        }
    }

    suspend fun deletePromotion(promoId: String) {
        try {
            promotionDao.deletePromotionById(promoId)
            firestore.collection("promotions").document(promoId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}