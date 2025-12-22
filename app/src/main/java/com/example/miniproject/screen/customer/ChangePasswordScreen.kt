package com.example.miniproject.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.EditProfileViewModel
import com.example.miniproject.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: EditProfileViewModel,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val successColor = Color(0xFF4CAF50)

    val hasLength = viewModel.newPassword.value.length >= 6
    val hasUppercase = viewModel.newPassword.value.any { it.isUpperCase() }
    val hasLowercase = viewModel.newPassword.value.any { it.isLowerCase() }
    val hasDigit = viewModel.newPassword.value.any { it.isDigit() }
    val hasSymbol = viewModel.newPassword.value.any { !it.isLetterOrDigit() }
    val isPasswordValid = hasLength && hasUppercase && hasLowercase && hasDigit && hasSymbol


    // Reset fields when entering the screen
    LaunchedEffect(Unit) {
        viewModel.resetPasswordFields()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top, // Changed to Top to accommodate growing list
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Secure your account by updating your password.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Password
            OutlinedTextField(
                value = viewModel.currentPassword.value,
                onValueChange = { viewModel.currentPassword.value = it },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            OutlinedTextField(
                value = viewModel.newPassword.value,
                onValueChange = { viewModel.newPassword.value = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // Password Requirements List
            if (viewModel.newPassword.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    PasswordRequirementRow(
                        "At least 6 characters",
                        hasLength,
                        successColor
                    )
                    PasswordRequirementRow(
                        "Small & Capital Letters",
                        hasLowercase && hasUppercase,
                        successColor
                    )
                    PasswordRequirementRow(
                        "At least one number (0-9)",
                        hasDigit,
                        successColor
                    )
                    PasswordRequirementRow(
                        "At least one symbol (!@#$)",
                        hasSymbol,
                        successColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = viewModel.confirmPassword.value,
                onValueChange = { viewModel.confirmPassword.value = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = viewModel.newPassword.value != viewModel.confirmPassword.value && viewModel.confirmPassword.value.isNotEmpty(),
                supportingText = {
                    if (viewModel.newPassword.value != viewModel.confirmPassword.value && viewModel.confirmPassword.value.isNotEmpty()) {
                        Text("Passwords do not match")
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Update Button
            Button(
                onClick = {
                    viewModel.changePassword {
                        // --- ON SUCCESS ---
                        Toast.makeText(context, "Password updated. Please login again.", Toast.LENGTH_LONG).show()

                        // Logout and redirect
                        loginViewModel.logout()
                        navController.navigate("Login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isPasswordValid, // Only enabled when all requirements met
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B4CFF),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Text("Update Password")
            }

            // Error Message Display
            if (viewModel.message.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.message.value,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

