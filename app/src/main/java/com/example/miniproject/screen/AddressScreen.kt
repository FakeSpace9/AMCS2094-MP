package com.example.miniproject.screen

import android.R.attr.top
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
        viewModel.syncAddresses()
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
                        viewModel.newAddress()
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
                            viewModel.editAddress(address.addressId)
                            navController.navigate("edit_address")
                        },
                        onDelete = { viewModel.deleteAddress(address.addressId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddAddressButton {
                viewModel.newAddress()
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
fun AddressItem(
    address: AddressEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
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
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete address",
                tint = Color.Red
            )
        }

        Column {

            Text(
                text = "${address.fullName} • ${address.phone}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = address.addressLine1,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = address.postcode,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (address.isDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DEFAULT",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1A73E8),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


