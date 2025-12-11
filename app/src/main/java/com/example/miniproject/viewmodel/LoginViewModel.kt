package com.example.miniproject.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.AdminEntity
import com.example.miniproject.data.entity.CustomerEntity
import com.example.miniproject.repository.LoginRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {


    private val _customerState = MutableStateFlow<LoginStateCustomer>(LoginStateCustomer.Idle)
    val customerState: StateFlow<LoginStateCustomer> = _customerState

    private val _adminState = MutableStateFlow<LoginStateAdmin>(LoginStateAdmin.Idle)
    val adminState: StateFlow<LoginStateAdmin> = _adminState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _customerState.value = LoginStateCustomer.Loading

            val result = repository.login(email, password)
            if (result.isSuccess) {
                authPrefs.saveLogin(email, "customer")
                _customerState.value = LoginStateCustomer.Success(result.getOrNull()!!)
            } else {
                _customerState.value =
                    LoginStateCustomer.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
    fun adminLogin(email: String, password: String) {
        viewModelScope.launch {
            _adminState.value = LoginStateAdmin.Loading

            val result = repository.adminLogin(email, password)
            if (result.isSuccess) {
                authPrefs.saveLogin(email, "admin")
                _adminState.value = LoginStateAdmin.Success(result.getOrNull()!!)
            } else {
                _adminState.value =
                    LoginStateAdmin.Error(result.exceptionOrNull()?.message ?: "Admin login failed")
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            repository.logoutFirebase()
            authPrefs.clearLogin()

            _customerState.value = LoginStateCustomer.Idle
            _adminState.value = LoginStateAdmin.Idle
        }
    }



    fun signInWithGoogle(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("541829479012-sbpl0qkshld60vp42p8ep3q7he4fsg7h.apps.googleusercontent.com") // replace with your Firebase web client ID
            .requestEmail()
            .build()

        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, gso)

        val signInIntent: Intent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }

    fun handleGoogleLogin(idToken: String) {
        viewModelScope.launch {
            _customerState.value = LoginStateCustomer.Loading

            val result = repository.loginWithGoogle(idToken)

            if (result.isSuccess) {
                val user = result.getOrNull()!!

                authPrefs.saveLogin(user.email, "customer")

                _customerState.value = LoginStateCustomer.Success(user)
            } else {
                _customerState.value = LoginStateCustomer.Error("Google login failed")
            }
        }
    }
    fun restoreAdminSession(admin: AdminEntity) {
        _adminState.value = LoginStateAdmin.Success(admin)
    }

    fun restoreCustomerSession(customer: CustomerEntity) {
        _customerState.value = LoginStateCustomer.Success(customer)
    }
    fun checkSession() {
        viewModelScope.launch {
            if (!authPrefs.shouldAutoLogin()) return@launch

            val email = authPrefs.getLoggedInEmail() ?: return@launch
            val userType = authPrefs.getUserType() ?: return@launch

            if (userType == "customer") {
                _customerState.value = LoginStateCustomer.Loading
                val result = repository.getCustomerByEmail(email)
                _customerState.value = if (result.isSuccess) {
                    LoginStateCustomer.Success(result.getOrNull()!!)
                } else {
                    LoginStateCustomer.Error("Session expired, please login again")
                }
            } else if (userType == "admin") {
                _adminState.value = LoginStateAdmin.Loading
                val result = repository.getAdminByEmail(email)
                _adminState.value = if (result.isSuccess) {
                    LoginStateAdmin.Success(result.getOrNull()!!)
                } else {
                    LoginStateAdmin.Error("Session expired, please login again")
                }
            }
        }
    }
    fun clearMessages() {
        _customerState.value = LoginStateCustomer.Idle
        _adminState.value = LoginStateAdmin.Idle
    }

}

sealed class LoginStateCustomer {
    object Idle : LoginStateCustomer()
    object Loading : LoginStateCustomer()
    data class Success(val user: CustomerEntity) : LoginStateCustomer()
    data class Error(val message: String) : LoginStateCustomer()
}

sealed class LoginStateAdmin {
    object Idle : LoginStateAdmin()
    object Loading : LoginStateAdmin()
    data class Success(val admin: AdminEntity) : LoginStateAdmin()
    data class Error(val message: String) : LoginStateAdmin()
}
