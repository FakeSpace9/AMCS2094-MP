package com.example.miniproject.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.viewmodel.LoginStateCustomer
import com.example.miniproject.viewmodel.LoginViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val customerProfileState = viewModel.customerState.collectAsState().value

    val username = (customerProfileState as? LoginStateCustomer.Success)
        ?.user
        ?.name
        ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Profile",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = username, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))

        // ---------- Menu Items ----------
        ProfileMenuItem("My Orders") { navController.navigate("order_history") }
        ProfileMenuItem("Edit Profile") { navController.navigate("edit") }
        ProfileMenuItem("Shipping Address") { navController.navigate("address") }
        ProfileMenuItem("Payment Methods") { navController.navigate("payment") }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(thickness = 1.dp, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        // ---------- Logout ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.logout()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Go",
            tint = Color.Black
        )
    }
}
