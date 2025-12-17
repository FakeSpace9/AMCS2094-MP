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
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductState
import com.example.miniproject.viewmodel.VariantUiState
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun AdminEditProductForm(
    navController: NavController,
    viewModel: ProductFormViewModel,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val variants by viewModel.variants.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val existingImages by viewModel.existingImageUrls.collectAsState()
    val takenSkus by viewModel.takenSkus.collectAsState()

    val name by viewModel.productName.collectAsState()
    val desc by viewModel.productDesc.collectAsState()
    val cat by viewModel.category.collectAsState()
    val gend by viewModel.gender.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    LaunchedEffect(saveState) {
        if (saveState is ProductState.Success) {
            Toast.makeText(context, "Product Updated Successfully!", Toast.LENGTH_SHORT).show()
            onUpdateSuccess()
        } else if (saveState is ProductState.Error) {
            Toast.makeText(context, (saveState as ProductState.Error).message, Toast.LENGTH_LONG)
                .show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 70.dp)
        ) {
            item {
                Text(
                    "PRODUCT IMAGES",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    items(existingImages) { url ->
                        Box(
                            modifier = Modifier
                                .size(100.dp, 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.6f), CircleShape)
                                    .clickable { viewModel.removeExistingImage(url) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    items(selectedImages) { uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp, 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.6f), CircleShape)
                                    .clickable { viewModel.removeImage(uri) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    "PRODUCT DETAILS",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateProductName(it) }, // Calls Auto-SKU Logic
                    label = { Text("Product Name") },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomDropdownEdit(
                            "Category",
                            viewModel.allCategories,
                            cat
                        ) { viewModel.category.value = it }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CustomDropdownEdit(
                            "Department",
                            viewModel.allGenders,
                            gend
                        ) { viewModel.gender.value = it }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(variants) { variant ->
                val unavailableSizes = viewModel.getUnavailableSizes(variant.id, variant.colour)
                val isSkuTaken = takenSkus.contains(variant.sku.trim().uppercase())
                val isSkuDuplicate = variants.count {
                    it.sku.trim().uppercase() == variant.sku.trim()
                        .uppercase() && it.sku.isNotBlank()
                } > 1
                val skuError =
                    if (isSkuTaken) "Already Exists" else if (isSkuDuplicate) "Duplicate SKU" else null

                VariantCardEdit(
                    variantState = variant,
                    allSizes = viewModel.allSizes,
                    validColors = viewModel.validColors,
                    unavailableSizes = unavailableSizes,
                    skuErrorMessage = skuError,
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
                            if (it.rawValue != null) viewModel.updateVariant(variant.id) { v ->
                                v.sku = it.rawValue!!.uppercase()
                            }
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
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Another Variant")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.deleteProduct() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Product")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

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
                Text("Update Product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDropdownEdit(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp), modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariantCardEdit(
    variantState: VariantUiState,
    allSizes: List<String>,
    validColors: List<String>,
    unavailableSizes: List<String>,
    skuErrorMessage: String?,
    onUpdate: (VariantUiState) -> Unit,
    onRemove: () -> Unit,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (variantState.size.isEmpty()) "Variant Details" else "${variantState.colour} - ${variantState.size}",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        "Remove",
                        tint = Color.Red
                    )
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = variantState.colour,
                    onValueChange = { onUpdate(variantState.copy(colour = it)); expanded = true },
                    placeholder = { Text("Select Color") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Row {
                            if (variantState.colour.isNotEmpty()) {
                                IconButton(onClick = {
                                    onUpdate(variantState.copy(colour = "")); expanded = true
                                }) { Icon(Icons.Default.Close, "Clear") }
                            }; ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                val filtered = validColors.filter { it.contains(variantState.colour, true) }
                if (filtered.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        filtered.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    onUpdate(variantState.copy(colour = c)); expanded = false
                                })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allSizes.forEach { size ->
                    FilterChip(
                        selected = variantState.size == size,
                        onClick = { onUpdate(variantState.copy(size = size)) },
                        label = { Text(size) },
                        enabled = !unavailableSizes.contains(size),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(
                                0xFF1A1B2E
                            ),
                            selectedLabelColor = Color.White,
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val priceVal = variantState.price.toDoubleOrNull()
                val isPriceError =
                    variantState.price.isNotEmpty() && (priceVal == null || priceVal <= 0.0)
                OutlinedTextField(
                    value = variantState.price,
                    onValueChange = { onUpdate(variantState.copy(price = it)) },
                    placeholder = { Text("$ 0.00") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isPriceError,
                    supportingText = {
                        if (isPriceError) Text(
                            if (priceVal == null) "Invalid" else "Not 0",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        errorContainerColor = Color(0xFFFFF0F0),
                        errorBorderColor = Color.Red
                    )
                )

                val qtyVal = variantState.quantity.toIntOrNull()
                val isStockError =
                    variantState.quantity.isNotEmpty() && (qtyVal == null || qtyVal < 0)
                OutlinedTextField(
                    value = variantState.quantity,
                    onValueChange = { onUpdate(variantState.copy(quantity = it)) },
                    placeholder = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isStockError,
                    supportingText = {
                        if (isStockError) Text(
                            "Invalid",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        errorContainerColor = Color(0xFFFFF0F0),
                        errorBorderColor = Color.Red
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = variantState.sku,
                onValueChange = { onUpdate(variantState.copy(sku = it.uppercase())) },
                placeholder = { Text("SKU (Auto-Generated)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = onScanClick) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            "Scan",
                            tint = Color.Black
                        )
                    }
                },
                isError = skuErrorMessage != null,
                supportingText = {
                    if (skuErrorMessage != null) Text(
                        skuErrorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    errorContainerColor = Color(0xFFFFF0F0),
                    errorBorderColor = Color.Red
                )
            )
        }
    }
}