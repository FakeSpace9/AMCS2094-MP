package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

            // Customer Info
            Text(text = order.customerEmail, fontSize = 14.sp, color = Color.DarkGray)
            // Add address here if available in your entity

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(12.dp))

            // Footer: Total and Status Dropdown
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

                // --- Status Dropdown ---
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(getStatusColor(order.status).copy(alpha = 0.1f))
                            .border(1.dp, getStatusColor(order.status), RoundedCornerShape(8.dp))
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

// Helper function for colors
fun getStatusColor(status: String): Color {
    return when (status) {
        "New" -> Color(0xFF2196F3) // Blue
        "Processing" -> Color(0xFFFF9800) // Orange
        "Shipped" -> Color(0xFF9C27B0) // Purple
        "Completed" -> Color(0xFF4CAF50) // Green
        else -> Color.Gray
    }
}