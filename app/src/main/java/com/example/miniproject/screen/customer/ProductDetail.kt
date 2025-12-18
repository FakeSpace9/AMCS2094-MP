package com.example.miniproject.screen.customer

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.AddToCartStatus
import com.example.miniproject.viewmodel.ProductDetailScreenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    viewModel: ProductDetailScreenViewModel
) {
    val product by viewModel.product.collectAsState()
    val productImages by viewModel.productImages.collectAsState()
    val availableColours by viewModel.availableColours.collectAsState()
    val selectedColour by viewModel.selectedColour.collectAsState()
    val availableSizes by viewModel.availableSizes.collectAsState()
    val selectedSizes by viewModel.selectedSize.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val selectedVariant by viewModel.selectedVariant.collectAsState()
    val addToCartStatus by viewModel.addToCartStatus.collectAsState()
    val context = LocalContext.current

    var showSizeChart by remember { mutableStateOf(false) }

    LaunchedEffect(addToCartStatus) {
        when (addToCartStatus) {
            is AddToCartStatus.Success -> {
                Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartStatus()
            }
            is AddToCartStatus.Error -> {
                Toast.makeText(context, (addToCartStatus as AddToCartStatus.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(productId) {
        viewModel.loadProductData(productId)
    }

    if (showSizeChart && product != null) {
        SizeChartDialog(
            category = product!!.category,
            onDismiss = { showSizeChart = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.addToCart() },
                enabled = addToCartStatus !is AddToCartStatus.Loading && (selectedVariant?.stockQuantity ?: 0) > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (addToCartStatus is AddToCartStatus.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    val buttonText = if ((selectedVariant?.stockQuantity ?: 0) <= 0) "Out Of Stock" else "Add to cart"
                    Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Image Swipe ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    val pagerState = rememberPagerState(pageCount = { productImages.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(productImages[page])
                                    .crossfade(true)
                                    .build(),
                                contentDescription = product!!.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    if (productImages.size > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(productImages.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) PurpleAccent else Color.LightGray
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = product!!.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = priceRange,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleAccent
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- COLOUR SELECTION ---
                if (availableColours.isNotEmpty()) {
                    Text(
                        text = "Select Colour",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth() // Added fillMaxWidth for Left Alignment
                    ) {
                        items(availableColours) { colour ->
                            ColourCard(
                                colourText = colour,
                                isSelected = colour == selectedColour,
                                onClick = { viewModel.selectColour(colour) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                // ------------------------

                // --- SIZE SELECTION ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Size",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Size Guide",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurpleAccent,
                        modifier = Modifier.clickable { showSizeChart = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth() // Added fillMaxWidth for Left Alignment
                ) {
                    items(availableSizes) { size ->
                        SizeCard(
                            sizeText = size,
                            isSelected = size == selectedSizes,
                            onClick = { viewModel.selectSize(size) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // ---------------------

                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = product!!.description.ifEmpty { "No Description Available" },
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ColourCard(
    colourText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PurpleAccent else Color.White
    val textColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) PurpleAccent else Color.LightGray

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = colourText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun SizeCard(
    sizeText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PurpleAccent else Color.White
    val textColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) PurpleAccent else Color.LightGray

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sizeText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun SizeChartDialog(
    category: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${category.replaceFirstChar { it.uppercase() }} Size Chart",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                when (category.lowercase()) {
                    "tops" -> TopsSizeTable()
                    "bottoms" -> BottomsSizeTable()
                    else -> Text("No size guide available for this category.")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TopsSizeTable() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SizeTableRow("Size", "Chest (cm)", "Length (cm)", isHeader = true)
        HorizontalDivider()
        SizeTableRow("XS", "80-84", "60")
        HorizontalDivider()
        SizeTableRow("S", "84-88", "62")
        HorizontalDivider()
        SizeTableRow("M", "88-92", "64")
        HorizontalDivider()
        SizeTableRow("L", "92-96", "66")
        HorizontalDivider()
        SizeTableRow("XL", "96-100", "68")
    }
}

@Composable
fun BottomsSizeTable() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SizeTableRow("Size", "Waist (cm)", "Hip (cm)", isHeader = true)
        HorizontalDivider()
        SizeTableRow("XS", "60-64", "86-90")
        HorizontalDivider()
        SizeTableRow("S", "64-68", "90-94")
        HorizontalDivider()
        SizeTableRow("M", "68-72", "94-98")
        HorizontalDivider()
        SizeTableRow("L", "72-76", "98-102")
        HorizontalDivider()
        SizeTableRow("XL", "76-80", "102-106")
    }
}

@Composable
fun SizeTableRow(col1: String, col2: String, col3: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = col1,
            modifier = Modifier.weight(1f),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(
            text = col2,
            modifier = Modifier.weight(1f),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(
            text = col3,
            modifier = Modifier.weight(1f),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}