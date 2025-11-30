package com.example.miniproject.screen.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.miniproject.viewmodel.AddProductViewModel
import com.example.miniproject.viewmodel.ProductState
import com.example.miniproject.viewmodel.VariantUiState
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun AdminAddProductForm(
    navController: NavController,
    viewModel: AddProductViewModel
) {
    val context = LocalContext.current
    val variants by viewModel.variants.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    // Product Info State
    val name by viewModel.productName.collectAsState()
    val desc by viewModel.productDesc.collectAsState()
    val cat by viewModel.category.collectAsState()

    // Images State
    val selectedImages by viewModel.selectedImages.collectAsState()

    // 1. SETUP MULTIPLE IMAGE PICKER
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    // 2. SETUP BARCODE SCANNER CLIENT
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    // Handle Save State (Toast messages)
    LaunchedEffect(saveState) {
        when(saveState) {
            is ProductState.Success -> {
                Toast.makeText(context, "Product Added Successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProductState.Error -> {
                Toast.makeText(context, (saveState as ProductState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 70.dp) // Space for bottom button
        ) {
            // --- IMAGE SECTION (Multiple Images) ---
            item {
                Text("PRODUCT IMAGES", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. "Add Photos" Button (Always First)
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp, 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(width = 2.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                                Text("Add Photos", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    // 2. Selected Images List
                    items(selectedImages) { uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp, 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Remove Button (Small X)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .clickable { viewModel.removeImage(uri) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- GENERAL INFO SECTION ---
            item {
                Text("PRODUCT DETAILS", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.productName.value = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g. Vintage Denim Jacket") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.productDesc.value = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter product description...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CATEGORY", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cat,
                            onValueChange = { viewModel.category.value = it },
                            placeholder = { Text("Tops") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GENDER", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = "Unisex",
                            onValueChange = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledBorderColor = Color.Transparent,
                                disabledTextColor = Color.Black
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- DYNAMIC VARIANT CARDS SECTION ---
            items(variants) { variant ->
                // Check unavailable sizes SPECIFIC TO THIS COLOR
                val unavailableSizes = viewModel.getUnavailableSizes(variant.id, variant.colour)

                VariantCard(
                    variantState = variant,
                    allSizes = viewModel.allSizes,
                    unavailableSizes = unavailableSizes,
                    onUpdate = { updatedVariant ->
                        viewModel.updateVariant(variant.id) {
                            it.size = updatedVariant.size
                            it.colour = updatedVariant.colour
                            it.sku = updatedVariant.sku
                            it.price = updatedVariant.price
                            it.quantity = updatedVariant.quantity
                        }
                    },
                    onRemove = { viewModel.removeVariantCard(variant.id) },
                    onScanClick = {
                        // 3. TRIGGER SCANNER
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                val rawValue = barcode.rawValue
                                if (rawValue != null) {
                                    // Update the SKU for this specific variant
                                    viewModel.updateVariant(variant.id) {
                                        it.sku = rawValue
                                    }
                                    Toast.makeText(context, "Scanned: $rawValue", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Scan Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addVariantCard() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF573BFF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF573BFF))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Another Variant")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- SAVE BUTTON ---
        Button(
            onClick = { viewModel.saveProduct() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1B2E)),
            enabled = saveState != ProductState.Loading
        ) {
            if (saveState == ProductState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Save Product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantCard(
    variantState: VariantUiState,
    allSizes: List<String>,
    unavailableSizes: List<String>,
    onUpdate: (VariantUiState) -> Unit,
    onRemove: () -> Unit,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if(variantState.size.isEmpty()) "Variant" else "${variantState.colour} - ${variantState.size}",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))

            // COLOR
            Text("COLOR", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = variantState.colour,
                onValueChange = { onUpdate(variantState.copy(colour = it)) },
                placeholder = { Text("e.g. Red, Navy Blue") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // SIZE
            Text("SIZE", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allSizes.forEach { size ->
                    val isSelected = variantState.size == size
                    val isDisabled = unavailableSizes.contains(size)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdate(variantState.copy(size = size)) },
                        label = { Text(size) },
                        enabled = !isDisabled,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A1B2E),
                            selectedLabelColor = Color.White,
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Price & Quantity
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PRICE", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = variantState.price,
                        onValueChange = { onUpdate(variantState.copy(price = it)) },
                        placeholder = { Text("$ 0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("STOCK", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = variantState.quantity,
                        onValueChange = { onUpdate(variantState.copy(quantity = it)) },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // SKU / BARCODE
            Text("SKU / BARCODE", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = variantState.sku,
                onValueChange = { onUpdate(variantState.copy(sku = it)) },
                placeholder = { Text("Scan or enter SKU") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.Black)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }
    }
}