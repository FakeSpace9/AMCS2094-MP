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
fun AdminEditProductForm(
    navController: NavController,
    viewModel: AddProductViewModel
) {
    val context = LocalContext.current

    // --- Collect States from ViewModel ---
    val variants by viewModel.variants.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()      // New images selected from gallery
    val existingImages by viewModel.existingImageUrls.collectAsState()   // Old images from DB

    // Text Field States
    val name by viewModel.productName.collectAsState()
    val desc by viewModel.productDesc.collectAsState()
    val cat by viewModel.category.collectAsState()

    // 1. Setup Image Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    // 2. Setup Barcode Scanner
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    // Handle Success/Error Events
    LaunchedEffect(saveState) {
        if (saveState is ProductState.Success) {
            Toast.makeText(context, "Product Updated Successfully!", Toast.LENGTH_SHORT).show()
            // Optional: You can navigate back using navController.popBackStack()
        } else if (saveState is ProductState.Error) {
            Toast.makeText(context, (saveState as ProductState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 70.dp) // Leave space for the bottom button
        ) {
            item {
                Text("EDIT PRODUCT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // --- IMAGES SECTION (Mixed: Existing + New) ---
                Text("IMAGES", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Add Button
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp, 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                                Text("Add", color = Color.Gray)
                            }
                        }
                    }

                    // 2. Show EXISTING Images (From Firebase URL)
                    items(existingImages) { url ->
                        Box(modifier = Modifier.size(100.dp, 120.dp).clip(RoundedCornerShape(12.dp))) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Remove Existing Image Button (Small X)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.6f), CircleShape)
                                    .clickable { viewModel.removeExistingImage(url) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // 3. Show NEW Selected Images (From Local Uri)
                    items(selectedImages) { uri ->
                        Box(modifier = Modifier.size(100.dp, 120.dp).clip(RoundedCornerShape(12.dp))) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Remove New Image Button (Small X)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.6f), CircleShape)
                                    .clickable { viewModel.removeImage(uri) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- TEXT FIELDS ---
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.productName.value = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.productDesc.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cat,
                        onValueChange = { viewModel.category.value = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = "Unisex",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- VARIANTS LIST ---
            items(variants) { variant ->
                // Check sizes available for this specific color
                val unavailableSizes = viewModel.getUnavailableSizes(variant.id, variant.colour)

                VariantCardEdit(
                    variantState = variant,
                    allSizes = viewModel.allSizes,
                    unavailableSizes = unavailableSizes,
                    onUpdate = { updated ->
                        viewModel.updateVariant(variant.id) {
                            it.size = updated.size
                            it.colour = updated.colour
                            it.sku = updated.sku
                            it.price = updated.price
                            it.quantity = updated.quantity
                        }
                    },
                    onRemove = { viewModel.removeVariantCard(variant.id) },
                    onScanClick = {
                        scanner.startScan().addOnSuccessListener {
                            if (it.rawValue != null) viewModel.updateVariant(variant.id) { v -> v.sku = it.rawValue!! }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addVariantCard() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF573BFF))
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Variant")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- DELETE PRODUCT BUTTON (Exclusive to Edit Screen) ---
            item {
                OutlinedButton(
                    onClick = { viewModel.deleteProduct() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Product")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } // End LazyColumn

        // --- UPDATE BUTTON (Bottom Fixed) ---
        Button(
            onClick = { viewModel.saveProduct() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1B2E)),
            enabled = saveState != ProductState.Loading
        ) {
            if (saveState == ProductState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Update Product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantCardEdit(
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
                        )
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // SKU
            Text("SKU / BARCODE", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = variantState.sku,
                onValueChange = { onUpdate(variantState.copy(sku = it)) },
                placeholder = { Text("Scan or enter SKU") },
                modifier = Modifier.fillMaxWidth(),
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