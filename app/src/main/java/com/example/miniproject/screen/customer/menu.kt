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

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            TopHeader(currentTitle = if (selectedFilter == "All") "New Arrivals" else selectedFilter)
        },
        bottomBar = {
            BottomNavigationBar()
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
                    contentPadding = PaddingValues(bottom = 70.dp)
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
            PaginationRow()
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
fun TopHeader(currentTitle:String){
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
            IconButton(onClick = { /* Handle search icon click */ }) {
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
fun PaginationRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .background(Purple, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("1", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("2", color = BlueAccentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            "3",
            color = Color.Gray,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Medium
        )
        Text(
            "4",
            color = Color.Gray,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = { /* Next */ }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = Color.Gray)
        }
    }
}

@Composable
fun BottomNavigationBar(){
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
            onClick = { /* Handle navigation */ },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent,selectedIconColor = Color.Black,unselectedIconColor = Color.Gray)
        )
        NavigationBarItem(
            icon={ Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(28.dp)) },
            selected = false,
            onClick = { /* Handle navigation */ },
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
