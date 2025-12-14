package com.example.miniproject.screen

import android.R.attr.top
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.viewmodel.AddressViewModel

@Composable
fun AddressScreen(
    navController: NavController,
    viewModel: AddressViewModel
) {
    val addressList by viewModel.addresses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAddresses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.navigate("profile")
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Your Addresses",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (addressList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No address added yet.", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    AddAddressButton {
                        viewModel.startNewAddress()
                        navController.navigate("add_address")
                    }
                }
            }
        } else {
            LazyColumn {
                items(addressList) { address ->
                    AddressItem(
                        address = address,
                        onClick = {
                            viewModel.loadAddressForEdit(address.addressId)
                            navController.navigate("edit_address")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddAddressButton {
                viewModel.startNewAddress()
                navController.navigate("add_address")
            }
        }
    }
}

@Composable
fun AddAddressButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() }
            .background(Color.Blue, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Add New Address", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun AddressItem(address: AddressEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xF0F0F0), RoundedCornerShape(10.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text("${address.fullName} • ${address.phone}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${address.addressLine1}, ${address.addressLine2}")
        Text("${address.city}, ${address.state} ${address.postcode}")
        if (address.isDefault) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Default Address", color = Color.Red, fontSize = 12.sp)
        }
    }
}
