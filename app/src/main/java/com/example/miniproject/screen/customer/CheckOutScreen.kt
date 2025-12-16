package com.example.miniproject.screen.customer

import android.R
import android.R.attr.fontWeight
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.CheckoutViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import coil.util.CoilUtils.result
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.data.entity.PaymentEntity
import okhttp3.internal.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    navController: NavController,
    viewModel: CheckoutViewModel
){
    val context = LocalContext.current

    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val shippingFee = viewModel.shippingFee
    val discount by viewModel.discountAmount.collectAsState()

    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val selectedPayment by viewModel.selectedPayment.collectAsState()
    val orderState by viewModel.orderState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    val currentBackStack = navController.currentBackStackEntry
    val savedStateHandle = currentBackStack?.savedStateHandle
    // Observe Address Selection
    val selectedAddrId by savedStateHandle?.getStateFlow<Long?>("selected_address_id", null)?.collectAsState() ?: mutableStateOf(null)
    LaunchedEffect(selectedAddrId) {
        selectedAddrId?.let { id ->
            viewModel.selectAddressById(id)
            savedStateHandle?.remove<Long>("selected_address_id")
        }
    }

    // Observe Payment Selection
    val selectedPayId by savedStateHandle?.getStateFlow<Long?>("selected_payment_id", null)?.collectAsState() ?: mutableStateOf(null)
    LaunchedEffect(selectedPayId) {
        selectedPayId?.let { id ->
            viewModel.selectPaymentById(id)
            savedStateHandle?.remove<Long>("selected_payment_id")
        }
    }

    LaunchedEffect(orderState) {
        orderState?.let { result ->
            if (result.isSuccess) {
                val orderId = result.getOrNull() ?: 0L
                viewModel.resetOrderState()

                // Navigate using ID
                navController.navigate("order_success/$orderId") {
                    popUpTo("home") { inclusive = false }
                }
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                viewModel.resetOrderState()
            }
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("Checkout", fontWeight = FontWeight.Bold)},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if(cartItems.isNotEmpty()){
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ){
                            Text("Total To Pay", fontSize = 16.sp, color = Color.Gray)
                            Text(
                                "RM ${String.format("%.2f",grandTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {viewModel.payNow()},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                            enabled = selectedAddress !=null && selectedPayment !=null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text("Pay Now", fontSize = 16.sp,fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text("RM ${String.format("%.2f",grandTotal)}", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight,null,tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ){
        paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(cartItems) { items ->
                CheckOutProductCard(items)
            }

            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.Gray)
                        Text("RM ${String.format("%.2f", subtotal)}")
                    }
                    if (discount > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount", color = Color(0xFF4CAF50))
                            Text("-RM ${String.format("%.2f", discount)}", color = Color(0xFF4CAF50))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shipping", color = Color.Gray)
                        Text("RM ${String.format("%.2f", shippingFee)}")
                    }
                }
                Divider()
            }

            item {
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                PaymentSelectorCard(
                    payment = selectedPayment,
                    onClick = {
                        // Navigate to Payment Screen in SELECTION mode
                        navController.navigate("payment?selectMode=true")
                    }
                )
            }

            item {
                Text("Shipping Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                AddressSelectorCard(
                    address = selectedAddress,
                    onClick = {
                        // Navigate to Address Screen in SELECTION mode
                        navController.navigate("address?selectMode=true")
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun CheckOutProductCard(item: CartEntity){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        //image
        Box(
          modifier = Modifier
              .size(80.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ){
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.productImageUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(item.productName,fontWeight=FontWeight.Bold,fontSize = 16.sp)
                Icon(Icons.Default.DeleteOutline,null,tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Text(item.selectedColour,color=Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("x${item.quantity}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PaymentSelectorCard(payment: PaymentEntity?, onClick:()->Unit){
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ){
        // Icon
        val icon = if (payment?.paymentType == "TNG") Icons.Default.QrCode else Icons.Default.CreditCard
        Icon(icon, null, modifier = Modifier.size(28.dp), tint = Color.Black)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (payment != null) {
                Text(payment.displayName, fontWeight = FontWeight.SemiBold)
                val sub = if(payment.paymentType == "CARD") "**** ${payment.cardNumber?.takeLast(4)}" else payment.walletId ?: ""
                Text(sub, color = Color.Gray, fontSize = 12.sp)
            } else {
                Text("Select Payment Method", color = Color.Red)
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (payment != null) Color.LightGray else Color.Transparent)
                .border(1.dp, Color.LightGray, androidx.compose.foundation.shape.CircleShape)
        )
    }
}

@Composable
fun AddressSelectorCard(address: AddressEntity?, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (address != null) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("${address.label} (${address.fullName})", fontWeight = FontWeight.Bold)
                        Text(address.phone, color = Color.Gray, fontSize = 13.sp)
                        Text(
                            "${address.addressLine1}, ${address.postcode}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Shipping Address", color = Color.Red)
                }
            }
        }
    }
}