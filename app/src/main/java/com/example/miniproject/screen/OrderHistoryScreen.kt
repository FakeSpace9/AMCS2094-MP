package com.example.miniproject.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.OrderHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderHistoryViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Order History",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(viewModel.orders) { order ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            navController.navigate("order_detail/${order.id}")
                        }
                        .padding(16.dp)
                ) {
                    Column {

                        Text(
                            text = "Order #${order.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Date: ${formatOrderDate(order.orderDate)}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "Status: ${order.status}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "Total: RM %.2f".format(order.grandTotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun formatOrderDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    return sdf.format(Date(timestamp))
}
