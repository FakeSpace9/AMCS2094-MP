package com.example.miniproject.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.AddressViewModel

@Composable
fun AddAddressScreen(
    navController: NavController,
    viewModel: AddressViewModel
) {
    AddressFormContent(
        title = "Add Address",
        navController = navController,
        viewModel = viewModel
    )
}

@Composable
fun AddressFormContent(
    title: String,
    navController: NavController,
    viewModel: AddressViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = viewModel.fullName.value,
            onValueChange = { viewModel.fullName.value = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.phone.value,
            onValueChange = { viewModel.phone.value = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine1.value,
            onValueChange = { viewModel.addressLine1.value = it },
            label = { Text("Address Line 1") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine2.value,
            onValueChange = { viewModel.addressLine2.value = it },
            label = { Text("Address Line 2") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.city.value,
            onValueChange = { viewModel.city.value = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.state.value,
            onValueChange = { viewModel.state.value = it },
            label = { Text("State") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.postcode.value,
            onValueChange = { viewModel.postcode.value = it },
            label = { Text("Postcode") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.save { success ->
                    if (success) navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        if (viewModel.message.value.isNotEmpty()) {
            Text(
                text = viewModel.message.value,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
