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

    suspend fun syncPromotions() {
        try {
            val snapshot = firestore.collection("promotions").get().await()
            val promotions = snapshot.documents.mapNotNull { doc ->
                // Map Firestore fields back to Entity
                val id = doc.getString("promotionId") ?: return@mapNotNull null

                PromotionEntity(
                    promotionId = id,
                    adminId = doc.getString("adminId") ?: "",
                    code = doc.getString("code") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    discountRate = doc.getDouble("discountRate") ?: 0.0,
                    startDate = doc.getLong("startDate") ?: 0L,
                    endDate = doc.getLong("endDate") ?: 0L,
                    isPercentage = doc.getBoolean("isPercentage") ?: true
                )
            }

            if (promotions.isNotEmpty()) {
                promotionDao.replaceAllPromotions(promotions)
            } else {
                // If Firestore is empty, clear local DB too
                promotionDao.deleteAllPromotions()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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