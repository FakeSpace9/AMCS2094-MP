package com.example.miniproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminHomeScreen(
    userViewModel: UserViewModel,
    loginViewModel: LoginViewModel,
    navController: NavController
) {

    val currentUser by userViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUser()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()

            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, Admin!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Welcome, ${currentUser?.name ?: "Admin"}!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "logout", modifier = Modifier.clickable {
            loginViewModel.logout()
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        })
    }

}