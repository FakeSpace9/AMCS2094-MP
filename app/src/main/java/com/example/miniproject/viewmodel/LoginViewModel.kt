package com.example.miniproject.viewmodel

import LoginRepository
import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.UserEntity
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

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.login(email, password)
            if (result.isSuccess) {
                // Save login info
                authPrefs.saveLogin(email)

                _loginState.value = LoginState.Success(result.getOrNull()!!)
            } else {
                _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authPrefs.clearLogin()
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }


    fun checkAutoLogin() {
        if (authPrefs.shouldAutoLogin()) {
            val email = authPrefs.getLoggedInEmail()
            viewModelScope.launch {
                val user = repository.getUserByEmail(email!!)
                if (user != null) {
                    _loginState.value = LoginState.Success(user)
                }
            }
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
            _loginState.value = LoginState.Loading

            val result = repository.loginWithGoogle(idToken)

            if (result.isSuccess) {
                val user = result.getOrNull()!!

                authPrefs.saveLogin(user.email)

                _loginState.value = LoginState.Success(user)
            } else {
                _loginState.value = LoginState.Error("Google login failed")
            }
        }
    }


}



sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}
