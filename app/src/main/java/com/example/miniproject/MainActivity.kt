package com.example.miniproject

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
import com.example.miniproject.data.AppDatabase
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.EditProfileRepository
import com.example.miniproject.repository.SignupRepository
import com.example.miniproject.repository.ForgotPasswordRepository
import com.example.miniproject.repository.LoginRepository
import com.example.miniproject.screen.AddAddressScreen
import com.example.miniproject.screen.AddressScreen
import com.example.miniproject.screen.EditProfileScreen
import com.example.miniproject.screen.ForgotPasswordScreen
import com.example.miniproject.screen.HomeScreenWithDrawer
import com.example.miniproject.screen.LoginScreen
import com.example.miniproject.screen.ProfileScreen
import com.example.miniproject.screen.SignupScreen
import com.example.miniproject.screen.admin.AdminDashboardScreen
import com.example.miniproject.screen.admin.AdminLoginScreen
import com.example.miniproject.screen.admin.AdminProfileScreen
import com.example.miniproject.screen.admin.AdminSignupScreen
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.example.miniproject.viewmodel.AddProductViewModelFactory
import com.example.miniproject.viewmodel.AddressViewModel
import com.example.miniproject.viewmodel.AddressViewModelFactory
import com.example.miniproject.viewmodel.EditProfileViewModel
import com.example.miniproject.viewmodel.EditProfileViewModelFactory
import com.example.miniproject.viewmodel.ForgotPasswordViewModel
import com.example.miniproject.viewmodel.ForgotPasswordViewModelFactory
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.LoginViewModelFactory
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.SignupViewModel
import com.example.miniproject.viewmodel.SignupViewModelFactory
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
                    // 1. Initialize LoginViewModel
                    val authPrefs = AuthPreferences(this)
                    loginViewModel = viewModel(
                        factory = LoginViewModelFactory(
                            LoginRepository(
                                FirebaseAuth.getInstance(),
                                FirebaseFirestore.getInstance(),
                                AppDatabase.getInstance(this).CustomerDao(),
                                AppDatabase.getInstance(this).AdminDao()
                            ),
                            authPrefs
                        )
                    )

                    // 2. Trigger session check (loads data into ViewModel)
                    loginViewModel.checkSession()

                    // 3. Determine Start Destination based on Prefs (Immediate check)
                    val startDestination = if (authPrefs.shouldAutoLogin()) {
                        val type = authPrefs.getUserType()
                        if (type == "admin") "admin_dashboard" else "home"
                    } else {
                        "home"
                    }

                    App(
                        loginViewModel = loginViewModel,
                        startDest = startDestination
                    )
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
fun App(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel,
    startDest: String
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // --- Repositories & ViewModels ---
    val forgotPasswordRepository = ForgotPasswordRepository(FirebaseAuth.getInstance())
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModelFactory(forgotPasswordRepository)
    )

    val signupRepository = SignupRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )
    val signupViewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(signupRepository)
    )

    val db = AppDatabase.getInstance(context)
    val productFactory = AddProductViewModelFactory(
        firestore = FirebaseFirestore.getInstance(),
        storage = FirebaseStorage.getInstance("gs://miniproject-55de6.firebasestorage.app"),
        productDao = db.ProductDao()
    )
    val productFormViewModel: ProductFormViewModel = viewModel(factory = productFactory)
    val productSearchViewModel: ProductSearchViewModel = viewModel(factory = productFactory)

    val editProfileRepo = EditProfileRepository(
        customerDao = db.CustomerDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    val editProfileViewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModelFactory(
            editProfileRepo,
            AuthPreferences(context)
        )
    )

    val addressRepo = AddressRepository(
        addressDao = db.AddressDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    val addressViewModel: AddressViewModel = viewModel(
        factory = AddressViewModelFactory(addressRepo, AuthPreferences(context))
    )


    // --- Navigation Host ---
    NavHost(navController = navController, startDestination = startDest) {

        // --- CUSTOMER ROUTES ---
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
            ProfileScreen(navController = navController, viewModel = loginViewModel)
        }
        composable(route = "forgot_password") {
            ForgotPasswordScreen(
                navController = navController,
                forgotPasswordViewModel = forgotPasswordViewModel
            )
        }

        // --- ADMIN ROUTES ---
        composable("admin_signup") {
            AdminSignupScreen(navController = navController, viewModel = signupViewModel)
        }

        composable("admin_login") {
            AdminLoginScreen(
                navController = navController,
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("admin_login") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(
                navController = navController,
                formViewModel = productFormViewModel,
                searchViewModel = productSearchViewModel,
                loginViewModel = loginViewModel
            )
        }

        composable("admin_profile") {
            AdminProfileScreen(
                navController = navController,
                viewModel = loginViewModel
            )
        }

        composable("edit") {
            EditProfileScreen(
                viewModel = editProfileViewModel,
                navController = navController
            )
        }

        composable("address") {
            AddressScreen(
                navController = navController,
                viewModel = addressViewModel
            )
        }

        composable("add_address") {
            AddAddressScreen(viewModel = addressViewModel, navController = navController)
        }

    }
}