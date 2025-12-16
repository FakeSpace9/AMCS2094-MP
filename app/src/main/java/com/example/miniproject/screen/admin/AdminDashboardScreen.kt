package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag // Icon for items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.AnalyticsViewModel
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.PromotionViewModel
import com.example.miniproject.viewmodel.SalesHistoryViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    formViewModel: ProductFormViewModel,
    searchViewModel: ProductSearchViewModel,
    loginViewModel: LoginViewModel,
    promoViewModel: PromotionViewModel,
    salesViewModel: SalesHistoryViewModel,
    analyticsViewModel: AnalyticsViewModel
) {
    var selectedItem by remember { mutableIntStateOf(0) } // Default to POS (index 0) to see stats immediately
    val items = listOf(
        BottomNavItem("POS", Icons.Default.PointOfSale),
        BottomNavItem("Analytics", Icons.Default.Analytics),
        BottomNavItem("Orders", Icons.Default.ShoppingCart),
        BottomNavItem("Product", Icons.Default.Inventory)
    )

    // Collect Stats
    val todaySales by salesViewModel.todaySales.collectAsState()
    val todayOrders by salesViewModel.todayOrders.collectAsState()

    LaunchedEffect(Unit) {
        salesViewModel.syncOrders()
    }

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
                .background(Color(0xFFF8F9FA))
        ) {
            when (selectedItem) {
                0 -> {
                    // --- POS DASHBOARD TAB ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Today's Overview",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(24.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Sales Card
                            StatsCard(
                                title = "Sales",
                                value = "RM ${String.format("%.2f", todaySales)}",
                                icon = Icons.Default.Analytics,
                                color = Color(0xFF1565C0),
                                backgroundColor = Color(0xFFE3F2FD),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("admin_analytics") }
                            )

                            // Items Sold Card
                            StatsCard(
                                title = "Total Orders",
                                value = "$todayOrders",
                                icon = Icons.Default.ShoppingBag,
                                color = Color(0xFF2E7D32),
                                backgroundColor = Color(0xFFE8F5E9),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(48.dp))

                        // Start Button
                        Button(
                            onClick = { navController.navigate("admin_pos_scan") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF573BFF))
                        ) {
                            Icon(Icons.Default.PointOfSale, null)
                            Spacer(Modifier.width(12.dp))
                            Text("Start New POS Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> AdminAnalyticsScreen(navController, analyticsViewModel)
                2 -> PlaceholderScreen("Orders Feature Coming Soon")
                3 -> AdminProductSection(navController, formViewModel, searchViewModel, loginViewModel, promoViewModel)
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontSize = 14.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 20.sp, color = Color.Gray)
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector)