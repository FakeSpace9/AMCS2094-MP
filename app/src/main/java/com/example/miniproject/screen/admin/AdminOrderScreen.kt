package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.viewmodel.AdminOrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    navController: NavController,
    viewModel: AdminOrderViewModel
) {
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    val statusOptions = viewModel.statusOptions

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // --- Status Tabs ---
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF573BFF)
                    )
                }
            ) {
                statusOptions.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) Color(0xFF573BFF) else Color.Gray,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // --- Orders List ---
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found for this status.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        AdminOrderCard(
                            order = order,
                            statusOptions = statusOptions,
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(order.id, newStatus)
                            },
                            onClick = {
                                navController.navigate("admin_order_detail/${order.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: OrderEntity,
    statusOptions: List<String>,
    onStatusChange: (String) -> Unit,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = sdf.format(order.orderDate),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(text = order.customerEmail, fontSize = 14.sp, color = Color.DarkGray)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "RM ${String.format("%.2f", order.grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF573BFF)
                    )
                }

                // Status Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(getStatusColor(order.status).copy(alpha = 0.1f))
                            .border(1.dp, getStatusColor(order.status).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.status,
                            color = getStatusColor(order.status),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = getStatusColor(order.status)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        statusOptions.forEach { statusLabel ->
                            DropdownMenuItem(
                                text = { Text(statusLabel) },
                                onClick = {
                                    onStatusChange(statusLabel)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "New" -> Color(0xFF2962FF) // Blue
        "Processing" -> Color(0xFFFF9100) // Orange
        "Shipped" -> Color(0xFFAA00FF) // Purple
        "Completed" -> Color(0xFF00C853) // Green
        "Cancelled" -> Color(0xFFD50000) // Red
        else -> Color.Gray
    }
}