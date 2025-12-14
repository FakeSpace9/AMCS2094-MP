package com.example.miniproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.PaymentViewModel

@Composable
fun AddEditPaymentScreen(
    navController: NavController,
    viewModel: PaymentViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = if (viewModel.paymentId == null) "Add Payment Method" else "Edit Payment Method",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        PaymentTypeSelector(viewModel)
        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.paymentType.value == "CARD") {
            OutlinedTextField(
                value = viewModel.cardHolderName.value,
                onValueChange = { viewModel.cardHolderName.value = it },
                label = { Text("Card Holder Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.cardNumber.value,
                onValueChange = { viewModel.cardNumber.value = it },
                label = { Text("Card Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.expiryMonth.value,
                onValueChange = { viewModel.expiryMonth.value = it },
                label = { Text("Expiry Month (MM)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.expiryYear.value,
                onValueChange = { viewModel.expiryYear.value = it },
                label = { Text("Expiry Year (YY)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.cvv.value,
                onValueChange = { viewModel.cvv.value = it },
                label = { Text("CVV") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

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
        }

        if (viewModel.paymentType.value == "TNG") {
            OutlinedTextField(
                value = viewModel.walletId.value,
                onValueChange = { viewModel.walletId.value = it },
                label = { Text("Touch 'n Go Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

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
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.save { success ->
                    if (success) navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Payment Method")
        }
    }
}

@Composable
fun PaymentTypeSelector(viewModel: PaymentViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("CARD", "TNG")

    Column {
        Text("Select Payment Type", fontWeight = FontWeight.Medium)

        Box {
            OutlinedTextField(
                value = viewModel.paymentType.value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                label = { Text("Payment Type") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(270f) // make arrow point downward
                            .clickable { expanded = !expanded }
                    )
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.paymentType.value = type
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
