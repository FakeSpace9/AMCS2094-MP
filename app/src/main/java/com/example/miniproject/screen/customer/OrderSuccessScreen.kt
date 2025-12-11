package com.example.miniproject.screen.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.OrderSuccessViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OrderSuccessScreen(
    navController: NavController,
    orderId: Long,
    viewModel: OrderSuccessViewModel
) {
    val order by viewModel.order.collectAsState()
    val items by viewModel.orderItems.collectAsState()

    // Load data on entry
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    // Date Calculation
    val deliveryDate = remember {
        val today = LocalDate.now()
        val start = today.plusDays(2)
        val end = today.plusDays(3)
        val fmt = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
        "${start.format(fmt)} - ${end.format(fmt)}"
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { /* TODO: Track Order */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Track Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Continue Shopping",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Text("Order Confirmed", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF00C853), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(50.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Order Placed", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Order #${order?.id} | Just Now", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // Delivery Date
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Estimated Delivery Date", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(deliveryDate, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Order Items List
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Order Item(${items.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
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
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Size : ${item.size}", fontSize = 12.sp, color = Color.Gray)
                                Text("Price : RM ${String.format("%.2f", item.price)}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                // Shipping Address
                SectionRow("Shipping To", Icons.Default.LocationOn, order!!.deliveryAddress)
                Spacer(modifier = Modifier.height(16.dp))

                // Payment & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Payment", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(order!!.paymentMethod, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    Text(
                        text = "RM ${String.format("%.2f", order!!.grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(content, fontSize = 13.sp, color = Color.Gray)
        }
    }
}