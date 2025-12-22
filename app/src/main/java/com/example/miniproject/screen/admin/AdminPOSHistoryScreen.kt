package com.example.miniproject.screen.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.SalesHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPOSHistoryScreen(
    navController: NavController,
    viewModel: SalesHistoryViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val orders by viewModel.displayedOrders.collectAsState()
    val selectedHistoryDate by viewModel.selectedHistoryDate.collectAsState()

    val sdfTime = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
    }
    val sdfDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
    }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Handle Date Selection
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.selectHistoryDate(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Initialize "Today" on first load
    LaunchedEffect(Unit) {
        if (selectedTab == 0) viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("POS Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- TABS ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF573BFF),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF573BFF)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Today's Orders") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("History") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- HISTORY DATE SELECTOR ---
            if (selectedTab == 1) {
                Card(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (selectedHistoryDate != null)
                                sdfDate.format(Date(selectedHistoryDate!!))
                            else "Select a Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // --- ORDER LIST ---
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 1 && selectedHistoryDate == null)
                            "Please select a date."
                        else "No orders found.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("admin_pos_edit/${order.id}") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(Modifier.width(8.dp))
                                        // Show full date if in History tab, else just Time
                                        val dateStr = if(selectedTab == 0) sdfTime.format(order.orderDate) else sdfDate.format(order.orderDate) + ", " + sdfTime.format(order.orderDate)
                                        Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        if (order.customerEmail.isNullOrBlank()) "Walk-in Customer" else order.customerEmail,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("RM ${String.format("%.2f", order.grandTotal)}", fontWeight = FontWeight.Bold, color = Color(0xFF573BFF))
                                    Text(order.status, fontSize = 12.sp, color = if(order.status == "Completed") Color(0xFF2E7D32) else Color.Gray)
                                }

                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}