package com.example.miniproject.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.R
import com.example.miniproject.viewmodel.EditProfileViewModel
import com.example.miniproject.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    loginViewModel: LoginViewModel, // <--- Added LoginViewModel
    navController: NavController
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.loadCurrentUser() }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showPredefinedDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onImageSelected(it) } }

    val predefinedAvatars = listOf(
        "default_1" to R.drawable.profile,
        "default_2" to R.drawable.ezgif_5114ed3efc72cedb,
        "default_3" to R.drawable.ezgif_54e78d35703e3376,
    )

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = 8.dp)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
                Text("Edit Profile", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Profile Picture Section ---
            Box(modifier = Modifier.size(100.dp).clickable { showImageSourceDialog = true }) {
                val currentPic = viewModel.profilePicture.value
                val modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color.Gray, CircleShape)

                if (currentPic != null && predefinedAvatars.any { it.first == currentPic }) {
                    Image(painterResource(predefinedAvatars.first { it.first == currentPic }.second), null, modifier = modifier, contentScale = ContentScale.Crop)
                } else if (currentPic != null) {
                    AsyncImage(model = currentPic, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop, placeholder = painterResource(R.drawable.profile))
                } else {
                    Image(painterResource(R.drawable.profile), null, modifier = modifier, contentScale = ContentScale.Crop)
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).background(Color.White, CircleShape).padding(4.dp)) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF5B4CFF), modifier = Modifier.size(16.dp))
                }
            }

            // --- Form Fields ---
            OutlinedTextField(
                value = viewModel.name.value, onValueChange = { viewModel.name.value = it },
                label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.email.value, onValueChange = {},
                label = { Text("Email") }, enabled = false, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.phone.value, onValueChange = { viewModel.phone.value = it },
                label = { Text("Phone Number") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
            )

            // --- Change Password Button ---
            Button(
                onClick = { showPasswordDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Change Password") }

            // --- Save Button (Disabled if not modified) ---
            Button(
                onClick = { viewModel.saveProfile { if (it) navController.popBackStack() } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = viewModel.isModified, // <--- Disabled State Logic
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B4CFF),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) { Text("Save Changes") }

            viewModel.message.value.takeIf { it.isNotEmpty() }?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }

    // --- Password Change Dialog ---
    if (showPasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = currentPass, onValueChange = { currentPass = it }, label = { Text("Current Password") }, singleLine = true)
                    OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.changePassword(currentPass, newPass) {
                        // --- ON SUCCESS ---
                        Toast.makeText(context, "Password updated. Please login again.", Toast.LENGTH_LONG).show()
                        showPasswordDialog = false

                        // 1. Call global logout from LoginViewModel
                        loginViewModel.logout()

                        // 2. Navigate to Login
                        navController.navigate("Login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") } }
        )
    }

    // ... (Image dialogs similar to before)
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Change Profile Picture") },
            text = { Column {
                TextButton(onClick = { showImageSourceDialog = false; galleryLauncher.launch("image/*") }) { Text("Choose from Gallery") }
                TextButton(onClick = { showImageSourceDialog = false; showPredefinedDialog = true }) { Text("Choose Avatar") }
            }},
            confirmButton = {}
        )
    }
    if (showPredefinedDialog) {
        AlertDialog(
            onDismissRequest = { showPredefinedDialog = false },
            title = { Text("Select Avatar") },
            text = { LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(predefinedAvatars) { (code, resId) ->
                    Image(painterResource(resId), null, modifier = Modifier.size(60.dp).clip(CircleShape).clickable { viewModel.onPredefinedImageSelected(code); showPredefinedDialog = false }, contentScale = ContentScale.Crop)
                }
            }},
            confirmButton = { TextButton(onClick = { showPredefinedDialog = false }) { Text("Cancel") } }
        )
    }
}