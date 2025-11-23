package com.example.miniproject

import LoginScreen
import SignupViewModel
import SignupViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.data.SignupRepository
import com.example.miniproject.screen.HomeScreenWithDrawer
import com.example.miniproject.screen.SignupScreen
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { App() }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val signupRepository = SignupRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    // Create ViewModel with Factory
    val signupViewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(signupRepository)
    )
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreenWithDrawer(navController = navController)

        }
        composable("Login"){
            LoginScreen(navController = navController)
        }
        composable(route = "Signup"){
            SignupScreen(navController = navController,viewModel = signupViewModel)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    App()
}