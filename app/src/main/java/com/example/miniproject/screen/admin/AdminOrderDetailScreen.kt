package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.viewmodel.AdminOrderDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: Long,
    viewModel: AdminOrderDetailViewModel,
    navController: NavController
) {
    val order by viewModel.order.collectAsState()
    val items by viewModel.orderItems.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF573BFF))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Card (ID, Date, Status)
                item {
                    OrderHeaderCard(order!!)
                }

                // 2. Customer Info Card
                item {
                    CustomerInfoCard(order!!)
                }

                // 3. Items List Title
                item {
                    Text(
                        "Ordered Items (${items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 4. Items List
                items(items) { item ->
                    AdminOrderItemCard(item)
                }

                // 5. Payment Summary
                item {
                    PaymentSummaryCard(order!!)
                }
            }
        }
    }
}

@Composable
fun OrderHeaderCard(order: com.example.miniproject.data.entity.OrderEntity) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val statusColor = getStatusColor(order.status)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order ID", fontSize = 12.sp, color = Color.Gray)
                    Text("#${order.id}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(sdf.format(order.orderDate), color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CustomerInfoCard(order: com.example.miniproject.data.entity.OrderEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            InfoRow(Icons.Default.Email, order.customerEmail)
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Default.LocationOn, order.deliveryAddress)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Color(0xFF573BFF), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun AdminOrderItemCard(item: OrderItemEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Size: ${item.size} | Color: ${item.color}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "SKU: ${item.variantSku}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Price & Qty
            Column(horizontalAlignment = Alignment.End) {
                Text("x${item.quantity}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "RM ${String.format("%.2f", item.price * item.quantity)}",
                    color = Color(0xFF573BFF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(order: com.example.miniproject.data.entity.OrderEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            SummaryRow("Subtotal", order.totalAmount)
            SummaryRow("Shipping Fee", order.shippingFee)
            SummaryRow("Discount", -order.discount, isDiscount = true)

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "RM ${String.format("%.2f", order.grandTotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF573BFF)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Method: ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(
            "RM ${String.format("%.2f", amount)}",
            color = if (isDiscount) Color.Red else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}