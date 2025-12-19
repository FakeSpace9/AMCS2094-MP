package com.example.miniproject.screen.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.viewmodel.ProductSearchViewModel

val DarkButtonColor = Color(0xFF1F2937)
val BlueAccentColor = Color(0xFF4F46E5)
val LightGrayBorder = Color(0xFFE5E7EB)
val Purple = Color(0xFF573BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewArrivalScreen(
    navController: NavController,
    viewModel: ProductSearchViewModel
){

    val searchResults by viewModel.searchResults.collectAsState()
    val selectedFilter by viewModel.selectedCategory.collectAsState()

    val gridState = rememberLazyGridState()


    LaunchedEffect(selectedFilter) {
        gridState.scrollToItem(0)
    }


    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            TopHeader(
                currentTitle = if (selectedFilter == "All") "All Products" else selectedFilter,
                onCartClick = {navController.navigate("cart")},navController = navController)
        },

        containerColor = Color.White

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter ->
                    viewModel.selectedCategory.value = newFilter
                    viewModel.loadProducts()
                }
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFF9FAFB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Products Found",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .simpleVerticalScrollbar(gridState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(searchResults) { product ->
                        CustomerProductCard(
                            product = product,
                            onClick = {
                                navController.navigate("productDetail/${product.product.productId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerProductCard(
    product: ProductSearchResult,
    onClick: () -> Unit
){
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .width(160.dp)
            .clickable{ onClick()}
    ){
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB)),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(product.product.imageUrl)
                            .error(android.R.drawable.ic_menu_gallery) // Show this if URL fails
                            .placeholder(android.R.drawable.ic_menu_gallery) // Show this while loading
                            .fallback(android.R.drawable.ic_menu_gallery) // Show this if URL is empty
                            .build()
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = product.product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.product.category,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "RM ${product.minPrice ?: 0.00}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                //Cart Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ){
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(currentTitle:String, onCartClick:()->Unit, navController: NavController){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = currentTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("home") {
                popUpTo("Login") { inclusive = true }
            }}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    tint = Color.Gray
                )
            }
        },
        actions = {
            IconButton(onClick =  onCartClick ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart",
                    tint = Color.Gray
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
        )
    )
}
@Composable
fun FilterRow(
    selectedFilter: String ,
    onFilterSelected: (String) -> Unit
){
    // REMOVED: "New Arrivals" from this list
    val filters =listOf(
        "All",
        "Best Sellers",
        "Tops",
        "Bottom",
        "Outerwear",
        "Dresses",
        "Accessories"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ){

        items(filters){ filterName ->
            val isSelected = filterName == selectedFilter
            val isAllButton = filterName == "All"

            // Define colors: Dark Blue for "All" when selected
            val backgroundColor = if (isAllButton && isSelected) {
                Color(0xFF001F54) // Dark Blue
            } else if (isSelected) {
                Color(0xFFF3F4F6)
            } else {
                Color.White
            }

            val contentColor = if (isAllButton && isSelected) {
                Color.White
            } else if (isSelected) {
                Color.Black
            } else {
                Color.Gray
            }

            val borderColor = if (isAllButton && isSelected) {
                Color(0xFF001F54) // Match background
            } else if (isSelected) {
                Color.Black
            } else {
                LightGrayBorder
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable { onFilterSelected(filterName) }
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = filterName,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}



@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyGridState,
    width: Dp = 6.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f),
    padding: Dp = 4.dp
): Modifier {
    // 1. Force alpha to 1f so it doesn't fade out
    val alpha = 1f

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        val totalItemsCount = state.layoutInfo.totalItemsCount

        // Prevent division by zero if list is empty
        if (totalItemsCount > 0) {
            val elementHeight = this.size.height / totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val visibleItemsCount = state.layoutInfo.visibleItemsInfo.size

            // Calculate height. If all items are visible, this becomes the full height.
            val scrollbarHeight = visibleItemsCount * elementHeight

            // 2. Removed the "if (needDrawScrollbar)" check. It now draws always.
            drawRoundRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx() - padding.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha,
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }
}