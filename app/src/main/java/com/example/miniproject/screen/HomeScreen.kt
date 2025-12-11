package com.example.miniproject.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.LoginStateCustomer
import com.example.miniproject.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreenWithDrawer(navController: NavController, viewModel: LoginViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.5f), // half transparent background
        drawerContent = {
            DrawerMenu(navController = navController, viewModel = viewModel)
        }
    ) {
        HomeScreen(
            navController = navController,
            onMenuClick = {
                scope.launch { drawerState.open() }
            },
            loginViewModel = viewModel
        )
    }
}

@Composable
fun HomeScreen(navController: NavController, onMenuClick: () -> Unit, loginViewModel: LoginViewModel) {
    val customerLoginState by loginViewModel.customerState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item { TopBar(onMenuClick, viewModel = loginViewModel, navController = navController) }
        item { MenuTabs() }
        item { SalesBanner() }

        item { ProductSection(title = "New Arrival →") }
        item { ProductSection(title = "Superman Collab →") }

        item {
            when (customerLoginState) {
                is LoginStateCustomer.Success -> {
                    val customer = (customerLoginState as LoginStateCustomer.Success).user
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Customer Name: ${customer.name}", fontSize = 20.sp)
                        Text(text = "Email: ${customer.email}", fontSize = 20.sp)
                        Text(text = "Phone: ${customer.phone}", fontSize = 20.sp)
                    }
                }
                else -> {
                }
            }
        }
    }
}

@Composable
fun DrawerMenu(navController: NavController, viewModel: LoginViewModel) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Menu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))

        Text("New Arrivals")
        Spacer(Modifier.height(12.dp))
        Text("Categories")
        Spacer(Modifier.height(12.dp))
        Text("Sales")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun TopBar(onMenuClick: () -> Unit, viewModel: LoginViewModel, navController: NavController) {
    val customerloginState by viewModel.customerState.collectAsState()

    // Check if user is logged in
    val isLoggedIn = customerloginState is LoginStateCustomer.Success

    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.clickable { onMenuClick() })
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.Search, contentDescription = "Search")
        Spacer(modifier = Modifier.weight(1f))
        Text("Shop Name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.clickable {
                if (isLoggedIn) {
                    navController.navigate("profile")
                } else {
                    navController.navigate("Login")
                }
            }
        )
    }
}

@Composable
fun MenuTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("New Arrivals ▼")
        Text("Categories ▼")
        Text("Sales ▼")
    }
}

@Composable
fun SalesBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFECECEC)),
        contentAlignment = Alignment.Center
    ) {
        Text("Sales Banner Here", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProductSection(title: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                ProductCard()
            }
        }
    }
}

@Composable
fun ProductCard() {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color.LightGray, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("PIC")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Details . . .", fontSize = 12.sp)
        Text("RM 800", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}