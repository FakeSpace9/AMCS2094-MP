package com.example.miniproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.miniproject.data.dao.AddressDao
import com.example.miniproject.data.dao.AdminDao
import com.example.miniproject.data.dao.CartDao
import com.example.miniproject.data.dao.CustomerDao
import com.example.miniproject.data.dao.PaymentDao
import com.example.miniproject.data.dao.ProductDao
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.data.entity.AdminEntity
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.data.entity.CustomerEntity
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductImageEntity
import com.example.miniproject.data.entity.ProductVariantEntity

@Database(
    entities = [
        CustomerEntity::class,
        AdminEntity::class,
        ProductEntity::class,
        ProductImageEntity::class,
        ProductVariantEntity::class,
        AddressEntity::class,
        CartEntity::class,
        PaymentEntity::class

    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun CustomerDao(): CustomerDao
    abstract fun AdminDao(): AdminDao

    abstract fun ProductDao(): ProductDao
    abstract fun AddressDao(): AddressDao
    abstract fun CartDao(): CartDao
    abstract fun PaymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
