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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miniproject.data.AppDatabase
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.AddressRepository
import com.example.miniproject.repository.CartRepository
import com.example.miniproject.repository.EditProfileRepository
import com.example.miniproject.repository.SignupRepository
import com.example.miniproject.repository.ForgotPasswordRepository
import com.example.miniproject.repository.LoginRepository
import com.example.miniproject.repository.OrderRepository
import com.example.miniproject.repository.POSRepository
import com.example.miniproject.repository.PaymentRepository
import com.example.miniproject.repository.PromotionRepository
import com.example.miniproject.screen.AddAddressScreen
import com.example.miniproject.screen.AddEditPaymentScreen
import com.example.miniproject.screen.AddressScreen
import com.example.miniproject.screen.EditProfileScreen
import com.example.miniproject.screen.ForgotPasswordScreen
import com.example.miniproject.screen.HomeScreenWithDrawer
import com.example.miniproject.screen.LoginScreen
import com.example.miniproject.screen.PaymentMethodScreen
import com.example.miniproject.screen.ProfileScreen
import com.example.miniproject.screen.SignupScreen
import com.example.miniproject.screen.admin.AdminDashboardScreen
import com.example.miniproject.screen.admin.AdminLoginScreen
import com.example.miniproject.screen.admin.AdminPOSDetailsScreen
import com.example.miniproject.screen.admin.AdminPOSScanScreen
import com.example.miniproject.screen.admin.AdminPOSSuccessScreen
import com.example.miniproject.screen.admin.AdminProfileScreen
import com.example.miniproject.screen.admin.AdminSignupScreen
import com.example.miniproject.screen.customer.CartScreen
import com.example.miniproject.screen.customer.CheckOutScreen
import com.example.miniproject.screen.customer.NewArrivalScreen
import com.example.miniproject.screen.customer.OrderSuccessScreen
import com.example.miniproject.screen.customer.ProductDetailScreen
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.example.miniproject.viewmodel.AddProductViewModelFactory
import com.example.miniproject.viewmodel.AddressViewModel
import com.example.miniproject.viewmodel.AddressViewModelFactory
import com.example.miniproject.viewmodel.AdminPOSViewModel
import com.example.miniproject.viewmodel.AdminPOSViewModelFactory
import com.example.miniproject.viewmodel.CartViewModel
import com.example.miniproject.viewmodel.CartViewModelFactory
import com.example.miniproject.viewmodel.EditProfileViewModel
import com.example.miniproject.viewmodel.EditProfileViewModelFactory
import com.example.miniproject.viewmodel.ForgotPasswordViewModel
import com.example.miniproject.viewmodel.ForgotPasswordViewModelFactory
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.LoginViewModelFactory
import com.example.miniproject.viewmodel.PaymentViewModel
import com.example.miniproject.viewmodel.PaymentViewModelFactory
import com.example.miniproject.viewmodel.ProductDetailScreenViewModel
import com.example.miniproject.viewmodel.ProductDetailScreenViewModelFactory
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.SignupViewModel
import com.example.miniproject.viewmodel.SignupViewModelFactory
import com.example.miniproject.viewmodel.CheckoutViewModel
import com.example.miniproject.viewmodel.CheckoutViewModelFactory
import com.example.miniproject.viewmodel.OrderSuccessViewModel
import com.example.miniproject.viewmodel.OrderSuccessViewModelFactory
import com.example.miniproject.viewmodel.PromotionViewModel
import com.example.miniproject.viewmodel.PromotionViewModelFactory
import com.example.miniproject.viewmodel.SalesHistoryViewModel
import com.example.miniproject.viewmodel.SalesHistoryViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.serialization.builtins.BooleanArraySerializer

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

    val cartRepository = CartRepository(db.CartDao())
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(cartRepository)
    )

    val productDetailViewModel: ProductDetailScreenViewModel = viewModel(
        factory = ProductDetailScreenViewModelFactory(productDao = db.ProductDao(), cartRepository)
    )

    val paymentRepo = PaymentRepository(
        paymentDao = db.PaymentDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(paymentRepo, AuthPreferences(context))
    )

    val orderRepo = OrderRepository(
        orderDao = db.OrderDao(),
        cartDao = db.CartDao(),
        productDao = db.ProductDao(), // PASS PRODUCT DAO HERE
        firestore = FirebaseFirestore.getInstance()
    )

    val posRepository = POSRepository(
        posOrderDao = db.POSOrderDao(),
        productDao = db.ProductDao(), // PASS PRODUCT DAO HERE
        firestore = FirebaseFirestore.getInstance()
    )

    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(
            cartRepository = cartRepository,
            addressRepository = addressRepo,
            paymentRepository = paymentRepo,
            orderRepository = orderRepo,
            authPreferences = AuthPreferences(context)
        )
    )

    val orderSuccessViewModel: OrderSuccessViewModel = viewModel(
        factory = OrderSuccessViewModelFactory(orderRepo)
    )

    val promoRepo = PromotionRepository(db.PromotionDao(), FirebaseFirestore.getInstance())

    val adminPOSViewModel: AdminPOSViewModel = viewModel(
        factory = AdminPOSViewModelFactory(
            productDao = db.ProductDao(),
            posRepository = posRepository,
            promotionRepository = promoRepo
        )
    )

    val promoViewModel: PromotionViewModel = viewModel(
        factory = PromotionViewModelFactory(promoRepo, AuthPreferences(context))
    )

    val salesHistoryViewModel: SalesHistoryViewModel = viewModel(
        factory = SalesHistoryViewModelFactory(posRepository)
    )

    // --- Navigation Host ---
    NavHost(navController = navController, startDestination = startDest) {

        // --- CUSTOMER ROUTES ---
        composable("home") {
            HomeScreenWithDrawer(navController = navController, viewModel = loginViewModel, searchViewModel = productSearchViewModel)
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
                loginViewModel = loginViewModel,
                promoViewModel = promoViewModel,
                salesViewModel = salesHistoryViewModel
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

        composable(
            route = "address?selectMode={selectMode}",
            arguments = listOf(navArgument("selectMode") { defaultValue = false })
        ) { backStackEntry ->
            val selectMode = backStackEntry.arguments?.getBoolean("selectMode") ?: false
            AddressScreen(
                navController = navController,
                viewModel = addressViewModel,
                selectMode = selectMode
            )
        }

        composable("add_address") {
            AddAddressScreen(viewModel = addressViewModel, navController = navController)
        }

        composable("edit_address") {
            AddAddressScreen(viewModel = addressViewModel, navController = navController)
        }

        composable("menu") {
            NewArrivalScreen(
                navController = navController,
                viewModel = productSearchViewModel
            )
        }

        composable("cart"){
            CartScreen(
                navController = navController,
                viewModel = cartViewModel
            )
        }

        composable("productDetail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?:""
            ProductDetailScreen(
                navController = navController,
                viewModel = productDetailViewModel,
                productId = productId
            )
        }

        composable(
            route = "payment?selectMode={selectMode}",
            arguments = listOf(navArgument("selectMode") { defaultValue = false })
        ) { backStackEntry ->
            val selectMode = backStackEntry.arguments?.getBoolean("selectMode") ?: false
            PaymentMethodScreen(
                navController = navController,
                viewModel = paymentViewModel,
                selectMode = selectMode
            )
        }

        // ADD payment
        composable("add_payment") {
            AddEditPaymentScreen(
                navController = navController,
                viewModel = paymentViewModel
            )
        }

        // EDIT payment
        composable("edit_payment") {
            AddEditPaymentScreen(
                navController = navController,
                viewModel = paymentViewModel
            )
        }
        //checkout
        composable("checkout") {
            CheckOutScreen(
                navController = navController,
                viewModel = checkoutViewModel
            )
        }

        composable(
            route = "order_success/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            OrderSuccessScreen(
                navController = navController,
                orderId = orderId,
                viewModel = orderSuccessViewModel
            )
        }

        composable("admin_pos_scan") {
            AdminPOSScanScreen(navController = navController, viewModel = adminPOSViewModel)
        }
        composable("admin_pos_details") {
            AdminPOSDetailsScreen(navController = navController, viewModel = adminPOSViewModel)
        }
        composable("admin_pos_success") {
            AdminPOSSuccessScreen(navController = navController, viewModel = adminPOSViewModel)
        }

    }
}