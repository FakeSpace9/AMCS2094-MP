package com.example.miniproject.screen.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.LoginStateAdmin
import com.example.miniproject.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val adminState by viewModel.adminState.collectAsState()
    val updateMessage by viewModel.updateMessage.collectAsState()
    val context = LocalContext.current

    // UI State
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    // Listen for update success message
    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("Success")) {
                isEditing = false
            }
            viewModel.clearUpdateMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (adminState is LoginStateAdmin.Success) {
                        IconButton(onClick = {
                            if (!isEditing) {
                                // Start Editing: Pre-fill fields
                                val admin = (adminState as LoginStateAdmin.Success).admin
                                editName = admin.name
                                editPhone = admin.phone
                                isEditing = true
                            } else {
                                // Cancel Editing
                                isEditing = false
                            }
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Cancel" else "Edit",
                                tint = if (isEditing) Color.Red else Color(0xFF573BFF)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (adminState) {
                is LoginStateAdmin.Success -> {
                    val admin = (adminState as LoginStateAdmin.Success).admin

                    // --- 1. Profile Avatar Header ---
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(3.dp, Color(0xFF573BFF), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isEditing) {
                        Text(
                            text = admin.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Administrator",
                            fontSize = 14.sp,
                            color = Color(0xFF573BFF),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(Color(0xFF573BFF).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Editing Profile...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- 2. Details Section ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Email (Always Read-only)
                            AdminProfileField(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = admin.email,
                                isEditable = false
                            )

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                            Spacer(Modifier.height(20.dp))

                            // Name
                            AdminProfileField(
                                icon = Icons.Default.AccountCircle,
                                label = "Full Name",
                                value = if (isEditing) editName else admin.name,
                                isEditable = isEditing,
                                onValueChange = { editName = it }
                            )

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                            Spacer(Modifier.height(20.dp))

                            // Phone
                            AdminProfileField(
                                icon = Icons.Default.Phone,
                                label = "Phone Number",
                                value = if (isEditing) editPhone else admin.phone,
                                isEditable = isEditing,
                                onValueChange = { editPhone = it },
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // --- 3. Action Buttons ---
                    AnimatedVisibility(
                        visible = isEditing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editPhone.isNotBlank()) {
                                    viewModel.updateAdminProfile(editName, editPhone)
                                } else {
                                    Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF573BFF)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(
                        visible = !isEditing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate("admin_login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Logout, null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Log Out",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF573BFF))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfileField(
    icon: ImageVector,
    label: String,
    value: String,
    isEditable: Boolean,
    onValueChange: (String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF573BFF), modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(16.dp))

        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF573BFF),
                    focusedLabelColor = Color(0xFF573BFF),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(2.dp))
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
        }
    }
}