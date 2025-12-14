package com.example.miniproject.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.viewmodel.PaymentViewModel

@Composable
fun PaymentMethodScreen(
    navController: NavController,
    viewModel: PaymentViewModel,
    selectMode: Boolean
) {
    val payments by viewModel.payments.collectAsState()

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

            Spacer(Modifier.width(16.dp))

            Text(
                text = "Your Payment Methods",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(24.dp))

        payments.forEach { payment ->
            PaymentItem(
                payment = payment,
                onClick = {
                    if (selectMode) {
                        // SELECTION LOGIC
                        navController.previousBackStackEntry?.savedStateHandle?.set("selected_payment_id", payment.paymentId)
                        navController.popBackStack()
                    } else {
                        // EDIT LOGIC
                        viewModel.editPayment(payment.paymentId)
                        navController.navigate("edit_payment")
                    }
                },
                onDelete = {
                    viewModel.delete(payment.paymentId) {}
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        AddPaymentButton {
            viewModel.startNew()
            navController.navigate("add_payment")
        }
    }
}

@Composable
fun AddPaymentButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() }
            .background(Color(0xFF1A73E8), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Add Payment Method",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PaymentItem(
    payment: PaymentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {
                Text(payment.displayName, fontWeight = FontWeight.Bold)

                if (payment.paymentType == "CARD") {
                    Text("Card ending ${payment.cardNumber?.takeLast(4)}")
                } else {
                    Text("Touch N Go: ${payment.walletId}")
                }

                if (payment.isDefault) {
                    Text(
                        "DEFAULT",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onDelete() }
            )
        }
    }
}
