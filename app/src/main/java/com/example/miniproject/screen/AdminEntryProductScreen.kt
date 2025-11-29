package com.example.miniproject.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.AdminProductViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductEntryScreen(navController: NavController, viewModel: AdminProductViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // --- State Variables ---
    var selectedTab by remember { mutableStateOf("Entry") }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSizes by remember { mutableStateOf(setOf<String>()) }

    // --- Barcode Scanner Setup ---
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    Scaffold(
        bottomBar = { AdminBottomNavBar() }, // Reusing your existing Nav Bar
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState) // <--- MAKES IT SCROLLABLE
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Tabs
            SegmentedControl(
                items = listOf("Search", "Entry", "Edit"),
                selectedItem = selectedTab,
                onSelectionChange = { newItem ->
                    selectedTab = newItem

                    // NAVIGATION LOGIC
                    if (newItem == "Search") {
                        navController.navigate("admin_home")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Photo Section
            Text("PRODUCT IMAGES", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Add Photo Box (Dashed Border)
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA))
                        .dashedBorder(Color.Gray, 12.dp)
                        .clickable { /* Logic to pick image */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                        Text("Add Photo", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Preview Box (Gray placeholder)
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Preview", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Basic Info
            LabelText("PRODUCT NAME")
            CustomInput(value = productName, onValueChange = { productName = it }, placeholder = "e.g. Vintage Denim Jacket")

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("CATEGORY")
                    CustomInput(value = category, onValueChange = { category = it }, placeholder = "Tops")
                }
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("GENDER")
                    CustomInput(value = gender, onValueChange = { gender = it }, placeholder = "Unisex")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Price & Stock & SKU Container
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            LabelText("PRICE")
                            CustomInput(
                                value = price,
                                onValueChange = { price = it },
                                placeholder = "$ 0.00",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            LabelText("STOCK QUANTITY")
                            CustomInput(
                                value = stock,
                                onValueChange = { stock = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LabelText("SKU / BARCODE")
                    // SKU Input with Scanner Button
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = { Text("Scan or enter SKU", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Gray
                        ),
                        trailingIcon = {
                            // --- SCANNER LOGIC HERE ---
                            IconButton(onClick = {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        // When scan is successful, put result in text field
                                        sku = barcode.rawValue ?: ""
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Scan failed", Toast.LENGTH_SHORT).show()
                                    }
                            }) {
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Scan", tint = Color.Black)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Color & Size
            LabelText("COLOR")
            CustomInput(value = color, onValueChange = { color = it }, placeholder = "e.g. Red, White")

            LabelText("AVAILABLE SIZES")
            SizeSelector(
                availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL"),
                selectedSizes = selectedSizes,
                onSizeSelected = { size ->
                    selectedSizes = if (selectedSizes.contains(size)) {
                        selectedSizes - size
                    } else {
                        selectedSizes + size
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Description
            LabelText("DESCRIPTION")
            CustomInput(
                value = description,
                onValueChange = { description = it },
                placeholder = "Enter product description...",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 8. Save Button
            Button(
                onClick = { /* Save Logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101D2E)) // Dark Navy
            ) {
                //Icon(painterResource(id = R.drawable.ic_cloud_upload), contentDescription = null) // Ensure you have an upload icon or remove
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// --- Helper Composable: Custom Input Field ---
@Composable
fun CustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Gray
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

// --- Helper Composable: Label ---
@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color.Gray,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// --- Helper Composable: Size Selector ---
@Composable
fun SizeSelector(
    availableSizes: List<String>,
    selectedSizes: Set<String>,
    onSizeSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        availableSizes.forEach { size ->
            val isSelected = selectedSizes.contains(size)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, if (isSelected) Color(0xFF101D2E) else Color.LightGray, CircleShape)
                    .background(if (isSelected) Color(0xFF101D2E) else Color.White)
                    .clickable { onSizeSelected(size) }
            ) {
                Text(
                    text = size,
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- Helper Modifier: Dashed Border ---
fun Modifier.dashedBorder(color: Color, cornerRadius: Dp) = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}