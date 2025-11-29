package com.example.miniproject.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R // Make sure this imports YOUR project's R file
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.UserViewModel

// --- Data Model for the List ---
data class ProductItemData(
    val name: String,
    val sku: String,
    val price: String,
    val stockCount: Int,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    loginViewModel: LoginViewModel
) {
    // --- 1. State & Logic Integration ---
    val currentUser by userViewModel.currentUser.collectAsState()
    var showMenu by remember { mutableStateOf(false) } // State for Logout menu

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUser()
    }

    // Sample Data
    val productList = listOf(
        ProductItemData("Denim Jacket", "9821321321", "RM 67.67", 12, R.drawable.ic_launcher_foreground),
        ProductItemData("Essential Cotton Tee", "6767962800", "RM 28.96", 98, R.drawable.ic_launcher_foreground),
        ProductItemData("TE's Pant", "1112123412", "RM 999.99", 1, R.drawable.ic_launcher_foreground)
    )

    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Search") }

    Scaffold(
        bottomBar = { AdminBottomNavBar() },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Header Row with Dynamic Name ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome,",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        // Dynamic Name from ViewModel
                        text = currentUser?.name ?: "Admin",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Profile Icon with Dropdown for Logout
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp),
                            tint = Color.DarkGray
                        )
                    }

                    // Logout Menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                loginViewModel.logout()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. Tabs ---
            SegmentedControl(
                items = listOf("Search", "Entry", "Edit"),
                selectedItem = selectedTab,
                onSelectionChange = { newItem ->
                    selectedTab = newItem

                    // NAVIGATION LOGIC
                    if (newItem == "Entry") {
                        navController.navigate("admin_entry")
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- 4. Search Bar (Fixed for Material 3) ---
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search products, SKU...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),

                // *** FIXED COLORS HERE ***
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Transparent,
                ),
                // *************************

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. Stats Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${productList.size} RESULTS",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "SORT BY: NEWEST",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 6. Product List ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productList) { product ->
                    ProductListItem(product)
                }
            }
        }
    }
}

// --- Component: Product Card ---
@Composable
fun ProductListItem(product: ProductItemData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = product.sku,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.price,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "${product.stockCount} left",
                            color = Color(0xFF2E7D32),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Component: Tabs ---
@Composable
fun SegmentedControl(
    items: List<String>,
    selectedItem: String,
    onSelectionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFFEEEEEE) else Color.Transparent)
                    .clickable { onSelectionChange(item) }
            ) {
                Text(
                    text = item,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.Gray
                )
            }
        }
    }
}

// --- Component: Bottom Nav ---
@Composable
fun AdminBottomNavBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.QrCodeScanner, contentDescription = "POS") },
            label = { Text("POS") },
            selected = false,
            onClick = { /* Navigate */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Analytics") },
            label = { Text("Analytics") },
            selected = false,
            onClick = { /* Navigate */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Inventory, contentDescription = "Orders") },
            label = { Text("Orders") },
            selected = false,
            onClick = { /* Navigate */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Product") },
            label = { Text("Product") },
            selected = true,
            onClick = { /* Navigate */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )
    }
}