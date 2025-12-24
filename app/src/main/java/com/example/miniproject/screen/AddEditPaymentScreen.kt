package com.example.miniproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.PaymentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddEditPaymentScreen(
    navController: NavController,
    viewModel: PaymentViewModel
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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

                Spacer(Modifier.width(16.dp))

                Text(
                    text = if (viewModel.paymentId == null)
                        "Add Payment Method"
                    else
                        "Edit Payment Method",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.paymentType.value,
                onValueChange = {},
                readOnly = true,
                label = { Text("Payment Type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (viewModel.paymentType.value == "CREDIT/DEBIT CARD") {

                OutlinedTextField(
                    value = viewModel.cardName.value.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Card Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.cardHolderName.value,
                    onValueChange = { viewModel.cardHolderName.value = it },
                    label = { Text("Card Holder Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.cardNumber.value,
                    onValueChange = { viewModel.detectCardNum(it) },
                    label = { Text("Card Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row {
                    OutlinedTextField(
                        value = viewModel.expiryMonth.value,
                        onValueChange = { viewModel.expiryMonth.value = it.filter(Char::isDigit) },
                        label = { Text("MM") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = viewModel.expiryYear.value,
                        onValueChange = { viewModel.expiryYear.value = it.filter(Char::isDigit) },
                        label = { Text("YY") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = viewModel.cvv.value,
                    onValueChange = { viewModel.cvv.value = it.filter(Char::isDigit) },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

            }

            if (viewModel.paymentType.value == "TNG") {
                OutlinedTextField(
                    value = viewModel.walletId.value,
                    onValueChange = { viewModel.walletId.value = it.filter(Char::isDigit) },
                    label = { Text("Touch 'n Go Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            viewModel.errorMessage.value?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.savePayment { success ->
                        if (success) {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Payment Method Saved"
                                )
                                delay(600)
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B4CFF),
                    contentColor = Color.White
                )
            ) {
                Text("Save Payment Method")
            }

        }
    }
}



