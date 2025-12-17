package com.example.miniproject.screen.admin

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.SalesHistoryViewModel
import java.util.Locale

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

    // Form State
    var email by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    // Discount Text State
    var discountText by remember { mutableStateOf("") }

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
            // Initial Discount Text
            discountText = if (it.discount == 0.0) "" else String.format(Locale.US, "%.2f", it.discount)
        }
    }

    // 3. Sync ViewModel Discount to Text Field (Auto-Calculation Support)
    LaunchedEffect(editDiscount) {
        val currentInput = discountText.toDoubleOrNull() ?: 0.0
        // Only update text if value is significantly different (avoids cursor jumping)
        if (kotlin.math.abs(currentInput - editDiscount) > 0.01) {
            discountText = if (editDiscount == 0.0) "" else String.format(Locale.US, "%.2f", editDiscount)
        }
    }

    // 4. Handle Save Success/Failure
    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()

            // FIX: Check for "Updated" OR "Success" to ensure navigation happens
            if (it.contains("Success") || it.contains("Updated")) {
                navController.popBackStack()
            }
        }
    }

    if (order == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                    IconButton(onClick = {
                        viewModel.saveOrderChanges(currentOrder, email, paymentMethod, status)
                    }) {
                        Icon(Icons.Default.Save, "Save", tint = Color(0xFF573BFF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Customer Info ---
            Text("Customer Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Customer Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // --- Order Items ---
            Text("Order Items", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.SemiBold)
                                Text("${item.size} | ${item.color}", fontSize = 12.sp, color = Color.Gray)
                                Text("RM ${item.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.updateItemQty(item, -1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Gray)
                                }

                                Text(
                                    text = "${item.quantity}",
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { viewModel.updateItemQty(item, 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF573BFF))
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.removeItem(item) }
                            ) {
                                Icon(Icons.Default.Delete, "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Totals ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Subtotal
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", modifier = Modifier.align(Alignment.CenterVertically))
                        Text("RM ${String.format("%.2f", editTotal)}", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Editable Discount Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Discount (RM)", color = Color.Gray)

                        OutlinedTextField(
                            value = discountText,
                            onValueChange = {
                                discountText = it
                                // Manual Edit: Updates ViewModel & Ratio
                                val newDiscount = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateDiscount(newDiscount)
                            },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF573BFF),
                                unfocusedContainerColor = Color.White
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End)
                        )
                    }

                    Divider(Modifier.padding(vertical = 12.dp))

                    // Grand Total
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "RM ${String.format("%.2f", editGrandTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF573BFF)
                        )
                    }
                }
            }
        }
    }
}