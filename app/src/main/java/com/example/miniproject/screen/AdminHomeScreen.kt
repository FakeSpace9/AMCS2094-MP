package com.example.miniproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.LoginStateAdmin
import com.example.miniproject.viewmodel.LoginViewModel

@Composable
fun AdminHomeScreen(
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    val adminState by loginViewModel.adminState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Admin Home",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (adminState) {

            is LoginStateAdmin.Loading -> {
                Text(text = "Loading admin data...")
            }

            is LoginStateAdmin.Error -> {
                val message = (adminState as LoginStateAdmin.Error).message
                Text(text = "Error: $message", color = Color.Red)
            }

            is LoginStateAdmin.Success -> {
                val admin = (adminState as LoginStateAdmin.Success).admin

                Text(text = "Admin Name: ${admin.name}", fontSize = 20.sp)
                Text(text = "Email: ${admin.email}", fontSize = 20.sp)
                Text(text = "Role: Administrator", fontSize = 20.sp)
            }

            LoginStateAdmin.Idle -> {
                Text(text = "No admin logged in.")
            }
        }
        Text(
            text = "Logout", modifier = Modifier.clickable {
                loginViewModel.logout()
                navController.navigate("home")
            })
    }
}

