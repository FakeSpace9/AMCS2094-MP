package com.example.miniproject.screen.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectShippingAddressScreen(
    navController: NavController,
    viewModel: AddressViewModel
) {
    val addresses by viewModel.addresses.collectAsState()

    // Ensure we have the latest data
    LaunchedEffect(Unit) {
        viewModel.loadAddresses()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Shipping Address", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Optional: Shortcut to Add Address if the list is empty or user wants a new one
                    IconButton(onClick = {
                        viewModel.newAddress()
                        navController.navigate("add_address")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add New", tint = PurpleAccent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->

        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No addresses found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(addresses) { address ->
                    SelectableAddressCard(
                        address = address,
                        onSelect = {
                            // 1. Pass the Selected ID back to Checkout
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_address_id", address.addressId)

                            // 2. Pop back to Checkout
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableAddressCard(
    address: AddressEntity,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PurpleAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(PurpleAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${address.addressLine1}, ${address.postcode}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Text(
                    text = address.phone,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Select",
                tint = Color.LightGray.copy(alpha = 0.5f) // Subtle hint that it's clickable
            )
        }
    }
}