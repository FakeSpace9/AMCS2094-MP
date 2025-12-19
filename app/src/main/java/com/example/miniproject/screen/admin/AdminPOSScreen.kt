package com.example.miniproject.screen.admin

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.AdminPOSViewModel
import com.example.miniproject.viewmodel.POSItem
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPOSScanScreen(
    navController: NavController,
    viewModel: AdminPOSViewModel
) {
    val items = viewModel.posItems
    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }
    var manualSku by remember { mutableStateOf("") }
    val message by viewModel.message.collectAsState()

    // Show errors toast
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Point Of Sales", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (items.isNotEmpty()) navController.navigate("admin_pos_details")
                    else Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Manual Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualSku,
                    onValueChange = { manualSku = it.uppercase() },
                    placeholder = { Text("Enter SKU Manually") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (manualSku.isNotBlank()) {
                            viewModel.onScanSku(manualSku)
                            manualSku = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(50.dp).height(50.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scan Button
            Button(
                onClick = {
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            barcode.rawValue?.let { viewModel.onScanSku(it) }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Scan Cancelled", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(Modifier.width(8.dp))
                Text("SCAN BARCODE")
            }

            Spacer(Modifier.height(24.dp))
            Text("Scanned Items (${items.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { item ->
                    POSItemRow(item,
                        onInc = { viewModel.updateQuantity(item, item.quantity + 1) },
                        onDec = { viewModel.updateQuantity(item, item.quantity - 1) }
                    )
                }
            }
        }
    }
}

@Composable
fun POSItemRow(item: POSItem, onInc: () -> Unit, onDec: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray) // Background while loading
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("SKU: ${item.sku}", fontSize = 12.sp, color = Color.Gray)
                Text("${item.color} | ${item.size}", fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("RM ${item.price}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveCircleOutline, null,
                        modifier = Modifier.size(24.dp).clickable { onDec() }, tint = Color.Gray)
                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                    Icon(Icons.Default.AddCircleOutline, null,
                        modifier = Modifier.size(24.dp).clickable { onInc() }, tint = PurpleAccent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPOSDetailsScreen(
    navController: NavController,
    viewModel: AdminPOSViewModel
) {
    val email by viewModel.customerEmail.collectAsState()
    val promo by viewModel.promoCode.collectAsState()

    val subTotal by viewModel.subTotal.collectAsState() // Original Total
    val discount by viewModel.discountAmount.collectAsState() // Discount Amount
    val total by viewModel.totalAmount.collectAsState() // Final Total

    val items = viewModel.posItems // List of items

    val paymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val message by viewModel.message.collectAsState() // For toast

    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(checkoutState) {
        checkoutState?.let { result ->
            if (result.isSuccess) {
                navController.navigate("admin_pos_success")
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Payment Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.completeOrder("ADMIN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Text("Complete Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enable scrolling for small screens
        ) {

            // --- 1. Order Summary (List of items) ---
            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.name} (x${item.quantity})", fontSize = 14.sp, color = Color.Gray)
                            Text("RM ${String.format("%.2f", item.price * item.quantity)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // --- 2. Customer Email ---
            Text("Customer Info", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.customerEmail.value = it },
                label = { Text("Customer Email (Receipt)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            // --- 3. Promo Code Section (With Button) ---
            Text("Discount", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = promo,
                    onValueChange = { viewModel.promoCode.value = it },
                    label = { Text("Promotion Code") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.applyPromoCode() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1B2E))
                ) {
                    Text("Apply")
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 4. Payment Calculation (Subtotal, Discount, Total) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = Color.Gray)
                    Text("RM ${String.format("%.2f", subTotal)}", fontWeight = FontWeight.Bold)
                }

                // Discount (Only show if there is one)
                if (discount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount", color = Color(0xFF00C853)) // Green
                        Text("- RM ${String.format("%.2f", discount)}", color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Payable", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("RM ${String.format("%.2f", total)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 5. Payment Method ---
            Text("Payment Method", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            val methods = listOf("Cash", "Credit Card", "E-Wallet / QR")
            methods.forEach { method ->
                // ... (Same as before) ...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            width = if(paymentMethod == method) 2.dp else 1.dp,
                            color = if(paymentMethod == method) PurpleAccent else Color.LightGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectedPaymentMethod.value = method }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when(method) {
                        "Cash" -> Icons.Default.Money
                        "Credit Card" -> Icons.Default.CreditCard
                        else -> Icons.Default.QrCode
                    }
                    Icon(icon, null, tint = Color.Black)
                    Spacer(Modifier.width(16.dp))
                    Text(method, fontWeight = FontWeight.Medium)
                }
            }
            // Add extra space at bottom for scrolling above the button
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdminPOSSuccessScreen(
    navController: NavController,
    viewModel: AdminPOSViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF00C853), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Spacer(Modifier.height(32.dp))
            Text("Order Placed Successfully", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))
            Text(
                "Receipt will be sent to customer email if provided.",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(64.dp))
            Button(
                onClick = {
                    viewModel.resetOrder()
                    navController.navigate("admin_dashboard") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}