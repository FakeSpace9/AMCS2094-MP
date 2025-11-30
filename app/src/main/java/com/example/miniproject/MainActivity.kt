package com.example.miniproject

import AppDatabase
import LoginRepository
import LoginScreen
import SignupViewModel
import SignupViewModelFactory
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.SignupRepository
import com.example.miniproject.screen.AdminHomeScreen
import com.example.miniproject.screen.AdminLoginScreen
import com.example.miniproject.screen.AdminSignupScreen
import com.example.miniproject.screen.HomeScreenWithDrawer
import com.example.miniproject.screen.SignupScreen
import com.example.miniproject.screen.SplashScreen
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.LoginViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    loginViewModel = viewModel(
                        factory = LoginViewModelFactory(
                            LoginRepository(
                                FirebaseAuth.getInstance(),
                                FirebaseFirestore.getInstance(),
                                AppDatabase.getInstance(this).CustomerDao(),
                                AppDatabase.getInstance(this).AdminDao()
                            ),
                            AuthPreferences(this)
                        )
                    )
                    loginViewModel.checkSession()
                    App(loginViewModel = loginViewModel)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginViewModel.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: return

                loginViewModel.handleGoogleLogin(idToken)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

@Composable
fun App(modifier: Modifier = Modifier,loginViewModel: LoginViewModel,) {
    val navController = rememberNavController()



    val signupRepository =
        SignupRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())


    // Create ViewModel with Factory
    val signupViewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(signupRepository)
    )




    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreenWithDrawer(navController = navController, viewModel = loginViewModel)

        }
        composable("Login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable(route = "Signup") {
            SignupScreen(navController = navController, viewModel = signupViewModel)
        }

        composable("admin_home") {
            AdminHomeScreen(
                loginViewModel = loginViewModel,
                navController = navController
            )
        }

        composable("admin_signup") {
            AdminSignupScreen(navController = navController, viewModel = signupViewModel)
        }
        composable("splash") {
            SplashScreen()
        }
        composable("adminLogin") {
            AdminLoginScreen(
                navController = navController,
                loginViewModel = loginViewModel
            )
        }


    }

}

