package com.example.miniproject.screen.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    formViewModel: ProductFormViewModel,
    searchViewModel: ProductSearchViewModel
) {
    var selectedItem by remember { mutableIntStateOf(3) } // Default to "Product" (index 3)
    val items = listOf(
        BottomNavItem("POS", Icons.Default.PointOfSale),
        BottomNavItem("Analytics", Icons.Default.Analytics),
        BottomNavItem("Orders", Icons.Default.ShoppingCart),
        BottomNavItem("Product", Icons.Default.Inventory)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF573BFF),
                            selectedTextColor = Color(0xFF573BFF),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> PlaceholderScreen("POS Feature Coming Soon")
                1 -> PlaceholderScreen("Analytics Feature Coming Soon")
                2 -> PlaceholderScreen("Orders Feature Coming Soon")
                3 -> AdminProductSection(navController, formViewModel, searchViewModel)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(text, fontSize = 20.sp, color = Color.Gray)
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector)