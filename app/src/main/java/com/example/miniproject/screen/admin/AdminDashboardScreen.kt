package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.AdminOrderViewModel
import com.example.miniproject.viewmodel.AnalyticsViewModel
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.PromotionViewModel
import com.example.miniproject.viewmodel.SalesHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    formViewModel: ProductFormViewModel,
    searchViewModel: ProductSearchViewModel,
    loginViewModel: LoginViewModel,
    promoViewModel: PromotionViewModel,
    salesViewModel: SalesHistoryViewModel,
    analyticsViewModel: AnalyticsViewModel,
    adminOrderViewModel: AdminOrderViewModel
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem("Home", Icons.Default.PointOfSale),
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
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF573BFF),
                            selectedTextColor = Color(0xFF573BFF),
                            indicatorColor = Color(0xFF573BFF).copy(alpha = 0.1f),
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
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                                actions = {
                                    IconButton(onClick = { navController.navigate("admin_profile") }) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = "Profile",
                                            tint = Color(0xFF573BFF),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                            )
                        },
                        containerColor = Color(0xFFF8F9FA)
                    ) { dashboardPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dashboardPadding)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Welcome Section
                            Text(
                                "Overview",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(Modifier.height(16.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Sales Card
                                StatsCard(
                                    title = "Today's Sales",
                                    value = "RM ${String.format("%.2f", todaySales)}",
                                    icon = Icons.Default.Analytics,
                                    color = Color(0xFF573BFF),
                                    backgroundColor = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                // Items Sold Card
                                StatsCard(
                                    title = "Orders Today",
                                    value = "$todayOrders",
                                    icon = Icons.Default.ShoppingBag,
                                    color = Color(0xFF00C853),
                                    backgroundColor = Color.White,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("admin_pos_history") }
                                )
                            }

                            Spacer(Modifier.height(32.dp))

                            Text(
                                "Quick Actions",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(Modifier.height(16.dp))

                            // Start POS Button
                            Button(
                                onClick = { navController.navigate("admin_pos_scan") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF573BFF)),
                                elevation = ButtonDefaults.buttonElevation(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PointOfSale, null, tint = Color.White)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("New Sale", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("Start Point of Sale", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // View History Button (Alternative)
                            Button(
                                onClick = { navController.navigate("admin_pos_history") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                elevation = ButtonDefaults.buttonElevation(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, null, tint = Color.Gray)
                                    Spacer(Modifier.width(12.dp))
                                    Text("View Sales History", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                1 -> AdminAnalyticsScreen(navController, analyticsViewModel)
                2 -> AdminOrdersScreen(navController, adminOrderViewModel)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector)