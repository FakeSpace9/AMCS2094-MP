package com.example.miniproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
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
            value = viewModel.postcode.value,
            onValueChange = { viewModel.postcode.value = it },
            label = { Text("Postcode") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }
        val options = listOf("Home", "Work", "Other")

        Box {
            OutlinedTextField(
                value = viewModel.label.value,
                onValueChange = {},
                label = { Text("Label") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                enabled = false,
                readOnly = true
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.label.value = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Set as default")
            Switch(
                checked = viewModel.isDefault.value,
                onCheckedChange = { viewModel.isDefault.value = it }
            )
        }

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
