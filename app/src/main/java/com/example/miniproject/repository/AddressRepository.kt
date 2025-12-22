package com.example.miniproject.repository

import com.example.miniproject.data.dao.AddressDao
import com.example.miniproject.data.entity.AddressEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddressRepository(
    private val addressDao: AddressDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun getAddressesByCustomerId(customerId: String): List<AddressEntity> {
        return addressDao.getAddressesByCustomerId(customerId)
    }

    suspend fun getAddressById(addressId: Long): AddressEntity? {
        return addressDao.getAddressById(addressId)
    }

    suspend fun saveAddress(address: AddressEntity): Result<Long> {
        return try {
            val id = if (address.addressId == 0L) {
                addressDao.insertAddress(address)
            } else {
                addressDao.updateAddress(address)
                address.addressId
            }

            val finalAddress = address.copy(addressId = id)

            val data = mapOf(
                "addressId" to finalAddress.addressId,
                "customerId" to finalAddress.customerId,
                "fullName" to finalAddress.fullName,
                "phone" to finalAddress.phone,
                "addressLine1" to finalAddress.addressLine1,
                "postcode" to finalAddress.postcode,
                "label" to finalAddress.label
            )

            val collection = firestore.collection("addresses")

            val snapshot = collection
                .whereEqualTo("customerId", finalAddress.customerId)
                .whereEqualTo("addressId", finalAddress.addressId)
                .get()
                .await()

            if (snapshot.isEmpty) {
                collection.add(data).await()
            } else {
                snapshot.documents.first().reference.set(data).await()
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }






    suspend fun deleteAddress(customerId: String, addressId: Long): Result<Unit> {
        return try {
            addressDao.deleteAddressById(addressId)

            firestore.collection("addresses")
                .whereEqualTo("customerId", customerId)
                .whereEqualTo("addressId", addressId)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.reference
                ?.delete()


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAddressesFromFirebase(customerId: String) {
        try {
            val snapshot = firestore.collection("addresses")
                .whereEqualTo("customerId", customerId)
                .get()
                .await()

            val addresses = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AddressEntity::class.java)
            }
            addressDao.deleteAddressByCustomerId(customerId)
            addressDao.insertAllAddress(addresses)

        } catch (e: Exception) {
        }
    }



}
