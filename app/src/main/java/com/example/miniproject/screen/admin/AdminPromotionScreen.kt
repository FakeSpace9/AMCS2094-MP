package com.example.miniproject.screen.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.miniproject.data.entity.PromotionEntity
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.PromotionViewModel

@Composable
fun AdminPromotionScreen(viewModel: PromotionViewModel) {
    val promotions by viewModel.promotions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (promotions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Active Promotions", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promotions) { promo ->
                    PromotionCard(promo, viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = PurpleAccent,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, null)
        }
    }

    if (showDialog) {
        AddPromotionDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun PromotionCard(promo: PromotionEntity, viewModel: PromotionViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(promo.code, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PurpleAccent)
                    Spacer(Modifier.width(8.dp))
                    Text("(${promo.discountRate}%)", fontWeight = FontWeight.Bold)
                }
                Text(promo.name, fontSize = 14.sp)
                Text(
                    "Valid: ${viewModel.formatDate(promo.startDate)} - ${viewModel.formatDate(promo.endDate)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { viewModel.deletePromo(promo.promotionId) }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromotionDialog(viewModel: PromotionViewModel, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("7") } // Default 7 days
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("New Promotion", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Promo Code (e.g. SALE50)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Campaign Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rate, onValueChange = { rate = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = duration, onValueChange = { duration = it },
                        label = { Text("Days Valid") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.createPromo(code, name, desc, rate, duration.toIntOrNull() ?: 7) { err ->
                            if (err == null) {
                                Toast.makeText(context, "Promotion Created!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Create")
                }
            }
        }
    }
}