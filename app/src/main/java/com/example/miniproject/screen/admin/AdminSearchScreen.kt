package com.example.miniproject.screen.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.viewmodel.AddProductViewModel
import com.example.miniproject.viewmodel.SortOption

@Composable
fun AdminSearchScreen(
    viewModel: AddProductViewModel,
    onProductClick: (ProductSearchResult) -> Unit // This is now "On Edit Click"
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.selectedSort.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(query, currentSort, viewModel.selectedCategory.collectAsState().value) {
        viewModel.loadProducts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search products...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF573BFF))
                }
            },
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF573BFF)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${searchResults.size} RESULTS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val sortText = when(currentSort) {
                SortOption.NEWEST -> "NEWEST"
                SortOption.PRICE_LOW_HIGH -> "PRICE: LOW - HIGH"
                SortOption.PRICE_HIGH_LOW -> "PRICE: HIGH - LOW"
                SortOption.NAME_A_Z -> "NAME A-Z"
            }
            Text("SORT BY: $sortText", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Product List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(searchResults) { product ->
                ProductSearchCard(
                    product = product,
                    onEditClick = { onProductClick(product) } // Pass edit action
                )
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(viewModel = viewModel, onDismiss = { showFilterDialog = false })
    }
}

@Composable
fun ProductSearchCard(
    product: ProductSearchResult,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // State for expansion

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            // Clicking the card toggles the "Details" view
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // --- TOP ROW (Summary) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Image
                Image(
                    painter = rememberAsyncImagePainter(product.product.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("SKU: ${product.displaySku ?: "-"}", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RM ${product.minPrice ?: 0.00}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0F7E0), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${product.totalStock ?: 0} left", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- EDIT BUTTON ---
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF573BFF), modifier = Modifier.size(20.dp))
                }
            }

            // --- EXPANDED SECTION (Details) ---
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Stock Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Grid-like display for variants
                    product.variants.forEach { variant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${variant.colour} - ${variant.size}",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Qty: ${variant.quantity}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (variant.quantity.toIntOrNull() ?: 0 > 0) Color.Black else Color.Red
                            )
                        }
                    }

                    // Collapse Hint
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ExpandLess, null, tint = Color.LightGray)
                    }
                }
            }

            // Expand Hint (Visible only when collapsed)
            if (!expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ExpandMore, null, tint = Color.LightGray)
                }
            }
        }
    }
}

// ... (FilterDialog remains the same) ...
@Composable
fun FilterDialog(
    viewModel: AddProductViewModel,
    onDismiss: () -> Unit
) {
    val currentSort by viewModel.selectedSort.collectAsState()
    val currentCategory by viewModel.selectedCategory.collectAsState()
    val categories = viewModel.getAvailableCategories()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Filter & Sort", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Sort Options
                Text("Sort By", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    SortOptionRow("Newest", SortOption.NEWEST, currentSort, viewModel)
                    SortOptionRow("Price: Low to High", SortOption.PRICE_LOW_HIGH, currentSort, viewModel)
                    SortOptionRow("Price: High to Low", SortOption.PRICE_HIGH_LOW, currentSort, viewModel)
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Category Filter
                Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedCategory.value = cat }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = currentCategory == cat,
                                onClick = { viewModel.selectedCategory.value = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF573BFF))
                            )
                            Text(text = cat, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1B2E))
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

@Composable
fun SortOptionRow(text: String, option: SortOption, current: SortOption, viewModel: AddProductViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectedSort.value = option }
            .padding(vertical = 6.dp)
    ) {
        RadioButton(
            selected = current == option,
            onClick = { viewModel.selectedSort.value = option },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF573BFF))
        )
        Text(text = text, fontSize = 16.sp)
    }
}