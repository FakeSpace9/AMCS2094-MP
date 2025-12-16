package com.example.miniproject.viewmodel

import android.R.attr.name
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.repository.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressViewModel(
    private val repo: AddressRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<AddressEntity>>(emptyList())
    val addresses: StateFlow<List<AddressEntity>> = _addresses

    var recipientName = mutableStateOf("")
    var addressPhone = mutableStateOf("")
    var addressLine1 = mutableStateOf("")
    var postcode = mutableStateOf("")
    var label = mutableStateOf("Home")
    var message = mutableStateOf("")

    private var currentCustomerId: String = ""
    private var editingAddressId: Long? = null

    init {
        loadAddresses()
    }

    fun isValidPostcode(postcode: String): Boolean {
        return Regex("^\\d{5}$").matches(postcode)
    }

    fun isValidPhone(phone: String): Boolean {
        return Regex("^01\\d{8,9}$").matches(phone)
    }


    fun validateAddress(): String? {
        if (recipientName.value.isBlank()) return "Name cannot be empty"
        if (addressLine1.value.isBlank()) return "Address cannot be empty"
        if (addressPhone.value.isBlank()) return "Phone Number cannot be empty"
        if (postcode.value.isBlank()) return "Postcode cannot be empty"
        if (!isValidPostcode(postcode.value)) return "Invalid Postcode"
        if (!isValidPhone(addressPhone.value)) return "Invalid Phone Number"


        return null
    }

    fun loadAddresses() {
        viewModelScope.launch {
            currentCustomerId = authPrefs.getUserId() ?: return@launch
            _addresses.value = repo.getAddressesByCustomerId(currentCustomerId)
        }
    }

    fun newAddress() {
        editingAddressId = null
        recipientName.value = ""
        addressPhone.value = ""
        addressLine1.value = ""
        postcode.value = ""
        label.value = "Home"
        message.value = ""
    }

    fun editAddress(addressId: Long) {
        viewModelScope.launch {
            val address = repo.getAddressById(addressId) ?: return@launch
            editingAddressId = address.addressId
            currentCustomerId = address.customerId

            recipientName.value = address.fullName
            addressPhone.value = address.phone
            addressLine1.value = address.addressLine1
            postcode.value = address.postcode
            label.value = address.label
            message.value = ""
        }
    }

    fun saveAddress(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {

            if (currentCustomerId.isEmpty()) {
                currentCustomerId = authPrefs.getUserId() ?: ""
            }

            val error = validateAddress()
            if (error != null) {
                message.value = error
                onResult(false)
                return@launch
            }

            val address = AddressEntity(
                addressId = editingAddressId ?: 0L,
                customerId = currentCustomerId,
                fullName = recipientName.value,
                phone = addressPhone.value,
                addressLine1 = addressLine1.value,
                postcode = postcode.value,
                label = label.value,
            )

            val result = repo.saveAddress(address)

            if (result.isSuccess) {
                loadAddresses()
                onResult(true)
            } else {
                message.value = "Failed: ${result.exceptionOrNull()?.message}"
                onResult(false)
            }
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            val result = repo.deleteAddress(currentCustomerId , addressId)
            if (result.isSuccess) {
                loadAddresses()

            }
        }
    }

    fun syncAddresses() {
        val userId = authPrefs.getUserId() ?: return

        viewModelScope.launch {
            repo.getAddressesFromFirebase(userId)
            loadAddresses()
        }
    }


}
