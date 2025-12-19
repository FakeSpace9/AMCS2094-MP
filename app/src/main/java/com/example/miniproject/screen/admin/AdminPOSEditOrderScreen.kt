package com.example.miniproject.screen.admin

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.miniproject.viewmodel.SalesHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPOSEditOrderScreen(
    navController: NavController,
    viewModel: SalesHistoryViewModel,
    orderId: Long
) {
    val order by viewModel.currentEditingOrder.collectAsState()
    val items by viewModel.editingItems.collectAsState()
    val editTotal by viewModel.editTotal.collectAsState()
    val editGrandTotal by viewModel.editGrandTotal.collectAsState()
    val editDiscount by viewModel.editDiscount.collectAsState()

    val updateMessage by viewModel.updateMessage.collectAsState()
    val context = LocalContext.current

    // Form State - Only Email is editable
    var email by remember { mutableStateOf("") }

    // Read-only display states
    var paymentMethod by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    // 1. Load Order Data
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }

    // 2. Sync Order Data to Local State
    LaunchedEffect(order) {
        order?.let {
            email = it.customerEmail ?: ""
            paymentMethod = it.paymentMethod
            status = it.status
        }
    }

    // 3. Handle Save Success/Failure
    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()

            if (it.contains("Success") || it.contains("Updated")) {
                navController.popBackStack()
            }
        }
    }

    if (order == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF573BFF))
        }
        return
    }

    val currentOrder = order!!

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Order #${currentOrder.id}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Profile Icon
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
        bottomBar = {
            // Save Button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Transparent)
            ) {
                Button(
                    onClick = {
                        // Pass original paymentMethod and status back since they aren't edited
                        viewModel.saveOrderChanges(currentOrder, email, paymentMethod, status)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF573BFF)),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save & Send Receipt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Customer Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    // Email (Editable)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Customer Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF573BFF),
                            focusedLabelColor = Color(0xFF573BFF),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Payment Method (Read Only)
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            label = { Text("Payment Method") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = false, // Disables interaction
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledBorderColor = Color.Transparent,
                                disabledTextColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            )
                        )

                        // Status (Read Only)
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            label = { Text("Status") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledBorderColor = Color.Transparent,
                                disabledTextColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Order Items (Read Only List with Images) ---
            Text("Order Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { itemState ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- Product Image (Correctly Referenced) ---
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(itemState.imageUrl) // <--- FIXED REFERENCE
                                    .crossfade(true)
                                    .build(),
                                contentDescription = itemState.data.productName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            )

                            Spacer(Modifier.width(16.dp))

                            // --- Item Details ---
                            Column(modifier = Modifier.weight(1f)) {
                                Text(itemState.data.productName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("${itemState.data.size} | ${itemState.data.color}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(4.dp))
                                Text("SKU: ${itemState.data.variantSku}", fontSize = 10.sp, color = Color.LightGray)
                            }

                            // --- Price & Qty ---
                            Column(horizontalAlignment = Alignment.End) {
                                Text("x${itemState.data.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "RM ${String.format("%.2f", itemState.data.price * itemState.data.quantity)}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF573BFF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Totals Summary (Read Only) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    // Subtotal
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.Gray)
                        Text("RM ${String.format("%.2f", editTotal)}", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Discount
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", color = Color.Gray)
                        Text(
                            "- RM ${String.format("%.2f", editDiscount)}",
                            color = if (editDiscount > 0) Color(0xFF00C853) else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider(Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    // Grand Total
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "RM ${String.format("%.2f", editGrandTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF573BFF)
                        )
                    }
                }
            }
        }
    }
}