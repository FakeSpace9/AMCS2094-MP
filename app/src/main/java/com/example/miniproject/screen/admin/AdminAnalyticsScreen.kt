package com.example.miniproject.screen.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.data.entity.AnalyticsTab
import com.example.miniproject.data.entity.BestSellerItem
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel
) {
    val stats by viewModel.stats.collectAsState()
    val currentTab by viewModel.selectedTab.collectAsState()
    val currentMonth by viewModel.selectedMonth.collectAsState()
    val currentYear by viewModel.selectedYear.collectAsState()

    // 1. Scroll State for the content column
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 2. Tab Selector (Fixed at the top)
            AnalyticsTabs(currentTab) { viewModel.setTab(it) }

            Spacer(Modifier.height(16.dp))

            // 3. Scrollable Content Area (Starts from Date Selectors)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining screen space
                    .verticalScroll(scrollState) // Enables scrolling for everything below
            ) {
                // Date Filters
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownSelector(
                        value = viewModel.months[currentMonth],
                        options = viewModel.months,
                        onSelect = { idx -> viewModel.setDate(idx, currentYear) },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownSelector(
                        value = currentYear.toString(),
                        options = viewModel.years,
                        onSelect = { idx -> viewModel.setDate(currentMonth, viewModel.years[idx].toInt()) },
                        modifier = Modifier.weight(0.5f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (stats == null) {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PurpleAccent)
                    }
                } else {
                    val data = stats!!

                    // Revenue Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Total Revenue", color = Color.Gray, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "RM ${String.format("%.2f", data.revenue)}",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Orders & Items Row
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AnalyticsStatCard("Total Orders", data.orders.toString(), "Orders Placed", Modifier.weight(1f))
                        AnalyticsStatCard("Item Sold", data.itemsSold.toString(), "Units Shipped", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    // Best Sellers Title
                    Text("Best Sellers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))

                    // Best Sellers List (Replaced LazyColumn with simple Loop for nested scrolling)
                    if (data.bestSellers.isEmpty()) {
                        Text("No sales data for this period", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        data.bestSellers.forEach { item ->
                            BestSellerCard(item)
                            Spacer(Modifier.height(12.dp)) // Spacing between items
                        }
                    }
                }

                // Bottom padding for comfortable scrolling
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ... (Helper functions below remain unchanged)

@Composable
fun AnalyticsTabs(selected: AnalyticsTab, onSelect: (AnalyticsTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        AnalyticsTab.values().forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.name.lowercase().capitalize(),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(title: String, value: String, sub: String, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BestSellerCard(item: BestSellerItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${item.totalQty} units sold", color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                "RM ${String.format("%.2f", item.totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF573BFF)
            )
        }
    }
}

@Composable
fun DropdownSelector(value: String, options: List<String>, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(value, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }