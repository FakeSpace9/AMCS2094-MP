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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.SignupRepository
import com.example.miniproject.repository.ForgotPasswordRepository
import com.example.miniproject.screen.ForgotPasswordScreen
import com.example.miniproject.screen.HomeScreenWithDrawer
import com.example.miniproject.screen.SignupScreen
import com.example.miniproject.screen.UserProfileScreen
import com.example.miniproject.screen.admin.AdminDashboardScreen
import com.example.miniproject.screen.admin.AdminLoginScreen
import com.example.miniproject.screen.admin.AdminSignupScreen
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.example.miniproject.viewmodel.AddProductViewModelFactory
import com.example.miniproject.viewmodel.ForgotPasswordViewModel
import com.example.miniproject.viewmodel.ForgotPasswordViewModelFactory
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.LoginViewModelFactory
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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
fun App(modifier: Modifier = Modifier, loginViewModel: LoginViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val forgotPasswordRepository = ForgotPasswordRepository(FirebaseAuth.getInstance())
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModelFactory(forgotPasswordRepository)
    )
    val signupRepository =
        SignupRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())


    // Create ViewModel with Factory
    val signupViewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(signupRepository)
    )

    val db = AppDatabase.getInstance(context) // Get Room Database instance

    // 1. Create the factory instance once
    val productFactory = AddProductViewModelFactory(
        firestore = FirebaseFirestore.getInstance(),
        storage = FirebaseStorage.getInstance("gs://miniproject-55de6.firebasestorage.app"),
        productDao = db.ProductDao()
    )

    // 2. Initialize productFormViewModel (Lower case 'p')
    val productFormViewModel: ProductFormViewModel = viewModel(factory = productFactory)

    // 3. Initialize productSearchViewModel (Missing in your code)
    val productSearchViewModel: ProductSearchViewModel = viewModel(factory = productFactory)

    // --- FIX ENDS HERE ---

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

        composable("profile") {
            UserProfileScreen(navController = navController, viewModel = loginViewModel)
        }

        composable("admin_signup") {
            AdminSignupScreen(navController = navController, viewModel = signupViewModel)
        }

        composable(route = "forgot_password"){
            ForgotPasswordScreen(navController = navController, forgotPasswordViewModel = forgotPasswordViewModel)
        }

        composable("admin_login") {
            AdminLoginScreen(
                navController = navController,
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    // Navigate to the new dashboard instead of admin_home
                    navController.navigate("admin_dashboard") {
                        popUpTo("admin_login") { inclusive = true }
                    }
                }
            )
        }

        // REPLACEMENT FOR "admin_home" and "add_product"
        composable("admin_dashboard") {
            AdminDashboardScreen(
                navController = navController,
                formViewModel = productFormViewModel, // Now matches the variable above
                searchViewModel = productSearchViewModel // Now exists
            )
        }
    }
}