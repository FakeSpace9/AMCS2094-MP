package com.example.miniproject.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.data.entity.OrderItemEntity
import com.example.miniproject.viewmodel.OrderHistoryViewModel
import com.google.common.math.LinearTransformation.vertical
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderDetailScreen(
    orderId: Long,
    viewModel: OrderHistoryViewModel,
    navController: NavController
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    val order = viewModel.selectedOrder
    val items = viewModel.orderItems

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Order Receipt",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))
        }

        HorizontalDivider()

        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {

            item {
                Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatDate(order.orderDate),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text("Delivery Address", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(order.deliveryAddress)
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

            }

            items(items) { item ->
                ReceiptItem(item)
                Spacer(Modifier.height(12.dp))
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                ReceiptRow("Subtotal", order.totalAmount)
                ReceiptRow("Shipping Fee", order.shippingFee)
                ReceiptRow("Discount", -order.discount)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                ReceiptRow(
                    title = "Grand Total",
                    value = order.grandTotal,
                    bold = true
                )

                HorizontalDivider(Modifier.padding(vertical = 12.dp))
            }

        }
    }
}

@Composable
fun ReceiptItem(item: OrderItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.productName,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Bold)
            Text(
                "Size: ${item.size} • Color: ${item.color}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                "Qty: ${item.quantity}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }

        Text(
            text = "RM %.2f".format(item.price * item.quantity),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReceiptRow(title: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            "RM %.2f".format(value),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
