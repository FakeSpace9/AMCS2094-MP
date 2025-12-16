package com.example.miniproject.screen.customer

import android.R.attr.fontWeight
import android.R.attr.text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.util.appendPlaceholders
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.screen.ProductCard
import com.example.miniproject.viewmodel.ProductSearchViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

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

    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 8

    LaunchedEffect(selectedFilter) {
        currentPage = 1
        viewModel.loadProducts()
    }

    // Calculate Pagination Data
    val totalItems = searchResults.size
    val totalPages = if (totalItems > 0) (totalItems + itemsPerPage - 1) / itemsPerPage else 1

    // Slice the list for the current page
    val currentItems = searchResults
        .drop((currentPage - 1) * itemsPerPage)
        .take(itemsPerPage)

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            TopHeader(
                currentTitle = if (selectedFilter == "All") "All Products" else selectedFilter,
                onCartClick = {navController.navigate("cart")})
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(searchResults) { product ->
                        CustomerProductCard(
                            product = product,
                            onClick = {
                                //Navigate to product detail
                                navController.navigate("productDetail/${product.product.productId}")
                            }
                        )
                    }
                }
            }
            // --- ADDED: Dynamic Pagination Row at the bottom ---
            if (searchResults.isNotEmpty()) {
                PaginationRow(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageChange = { newPage -> currentPage = newPage }
                )
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
fun TopHeader(currentTitle:String, onCartClick:()->Unit){
    CenterAlignedTopAppBar(
        title = {
            Text(
            text = currentTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* Handle navigation icon click */ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
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
    val filters =listOf(
        "All",
        "New Arrivals",
        "Best Sellers",
        "Tops",
        "Bottoms",
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
        item{
            Button(
                onClick = { /* Open Filter Modal */ },
                colors = ButtonDefaults.buttonColors(containerColor = DarkButtonColor),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ){
                Icon(
                    imageVector = Icons.Outlined.List,
                    contentDescription = "Filter",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filter",
                    fontSize = 14.sp,
                )
            }
        }
        items(filters){
            filterName ->
                val isSelected = filterName == selectedFilter
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Black else LightGrayBorder,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable { onFilterSelected(filterName) }
                    .background(if (isSelected) Color(0xFFF3F4F6) else Color.White)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = filterName,
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {if(currentPage > 1) onPageChange(currentPage - 1)}, enabled = currentPage > 1) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = Color.Gray
            )
        }

        val startPage = maxOf(1, currentPage - 2)
        val endPage = minOf(totalPages, startPage + 4)

        for (i in startPage..endPage) {
            val isSelected = i == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Purple else Color.Transparent)
                    .clickable { onPageChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$i",
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }

        IconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (currentPage < totalPages) Color.Gray else Color.LightGray
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController){
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon={
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            },
            selected = false,
            onClick = { navController.navigate("home"){popUpTo("home"){inclusive = true} } },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent,selectedIconColor = Color.Black,unselectedIconColor = Color.Gray)
        )
        NavigationBarItem(
            icon={ Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(28.dp)) },
            selected = false,
            onClick = { navController.navigate("search_screen") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent,selectedIconColor = Color.Black,unselectedIconColor = Color.Gray),
            label = { Text("Search") }
        )
        NavigationBarItem(
            icon={ Icon(Icons.Outlined.List, contentDescription = "List", tint = Color.Gray, modifier = Modifier.size(28.dp))},
            selected = false,
            onClick = { /* Handle navigation */ },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent,selectedIconColor = Color.Black,unselectedIconColor = Color.Gray),
            label = { Text("List") }
        )
    }
}
