package com.example.miniproject.viewmodel

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

    var fullName = mutableStateOf("")
    var phone = mutableStateOf("")
    var addressLine1 = mutableStateOf("")
    var addressLine2 = mutableStateOf("")
    var city = mutableStateOf("")
    var state = mutableStateOf("")
    var postcode = mutableStateOf("")
    var label = mutableStateOf("Home")
    var isDefault = mutableStateOf(false)
    var message = mutableStateOf("")

    private var currentCustomerId: String = ""
    private var editingAddressId: Long? = null

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            currentCustomerId = authPrefs.getLoggedInEmail() ?: return@launch
            _addresses.value = repo.getAddressesByCustomerId(currentCustomerId)
        }
    }

    fun startNewAddress() {
        editingAddressId = null
        fullName.value = ""
        phone.value = ""
        addressLine1.value = ""
        addressLine2.value = ""
        city.value = ""
        state.value = ""
        postcode.value = ""
        label.value = "Home"
        isDefault.value = false
        message.value = ""
    }

    fun loadAddressForEdit(addressId: Long) {
        viewModelScope.launch {
            val address = repo.getAddressById(addressId) ?: return@launch
            editingAddressId = address.addressId
            currentCustomerId = address.customerId

            fullName.value = address.fullName
            phone.value = address.phone
            addressLine1.value = address.addressLine1
            addressLine2.value = address.addressLine2
            city.value = address.city
            state.value = address.state
            postcode.value = address.postcode
            label.value = address.label
            isDefault.value = address.isDefault
            message.value = ""
        }
    }

    fun save(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (currentCustomerId.isEmpty()) {
                currentCustomerId = authPrefs.getLoggedInEmail() ?: ""
            }

            val address = AddressEntity(
                addressId = editingAddressId ?: 0L,
                customerId = currentCustomerId,
                fullName = fullName.value,
                phone = phone.value,
                addressLine1 = addressLine1.value,
                addressLine2 = addressLine2.value,
                city = city.value,
                state = state.value,
                postcode = postcode.value,
                label = label.value,
                isDefault = isDefault.value
            )

            val result = repo.saveAddress(address)

            if (result.isSuccess) {
                val savedId = result.getOrNull() ?: 0L
                if (isDefault.value && savedId != 0L) {
                    repo.setDefaultAddress(currentCustomerId, savedId)
                }
                message.value = "Address saved"
                loadAddresses()
                onResult(true)
            } else {
                message.value = "Failed: ${result.exceptionOrNull()?.message}"
                onResult(false)
            }
        }
    }
}
