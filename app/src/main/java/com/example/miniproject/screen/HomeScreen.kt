package com.example.miniproject.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.R
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.viewmodel.LoginStateCustomer
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.PromotionViewModel

@Composable
fun HomeScreenWithDrawer(navController: NavController,
                         viewModel: LoginViewModel,
                         searchViewModel: ProductSearchViewModel,
                         promoViewModel: PromotionViewModel
) {
    HomeScreen(
            navController = navController,
            loginViewModel = viewModel,
            searchViewModel = searchViewModel,
            promoViewModel = promoViewModel
        )

}
@Composable
fun HomeScreen(navController: NavController,
               loginViewModel: LoginViewModel,
               searchViewModel: ProductSearchViewModel,
               promoViewModel: PromotionViewModel
) {
    val customerLoginState by loginViewModel.customerState.collectAsState()
    val products by searchViewModel.searchResults.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        searchViewModel.searchQuery.value = ""

        // Add this line to reset the filter when returning to Home
        searchViewModel.selectedCategory.value = "All"

        promoViewModel.syncPromotions()
        searchViewModel.loadProducts()
    }


    // --- SECURE CLICK HANDLER ---
    val onProductClick: (String) -> Unit = { productId ->
        if (customerLoginState is LoginStateCustomer.Success) {
            navController.navigate("productDetail/$productId")
        } else {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            navController.navigate("Login")
        }
    }

    // --- UPDATED LAYOUT STRUCTURE ---
    // Use a Column to hold the fixed TopBar and the scrollable list
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Fixed TopBar (Stays at the top)
        TopBar( viewModel = loginViewModel, navController = navController)

        // 2. Scrollable Content (Takes up remaining space)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // This ensures it fills the space below TopBar
        ) {

            // Sales Banner
            item { SalesBanner() }

            item {
                CategoryRow(
                    categories = searchViewModel.getAvailableCategories().filter { it != "All" },
                    onCategoryClick = { category ->
                        searchViewModel.selectedCategory.value = category
                        navController.navigate("menu")
                    }
                )
            }

            // --- BEST SELLER GRID (Top 6, 2 per row) ---
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Best Seller",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            val bestSellers = products.take(6)

            items(bestSellers.chunked(2)) { rowProducts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onProductClick(product.product.productId)
                            }
                        )
                    }
                    // Handle odd number of items in the last row for alignment
                    val remainingSlots = 2 - rowProducts.size
                    if (remainingSlots > 0) {
                        Spacer(modifier = Modifier.weight(remainingSlots.toFloat()))
                    }
                }
            }

            // Bottom Spacer
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}


@Composable
fun TopBar( viewModel: LoginViewModel, navController: NavController) {
    val customerloginState by viewModel.customerState.collectAsState()

    // Check if user is logged in
    val isLoggedIn = customerloginState is LoginStateCustomer.Success

    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.clickable {
                if (isLoggedIn) {
                    navController.navigate("profile")
                } else {
                    navController.navigate("Login")
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text("E-Tire", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart",modifier = Modifier.clickable {
            if (isLoggedIn) {
                navController.navigate("cart")
            } else {
                navController.navigate("Login")
            }
        })
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.Search, contentDescription = "Search",modifier = Modifier.clickable {
            navController.navigate("search_screen")})

    }
}


@Composable
fun SalesBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFECECEC)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.promotionbanner),
            contentDescription = "Sales Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CategoryRow(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Shop by Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
fun CategoryCard(title: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6)) // Light Gray Background
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Category,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun ProductSection(title: String, onClick: () -> Unit = {}, products: List<ProductSearchResult>, navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            products.forEach { product ->
                ProductCard(
                    product = product,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate("productDetail/${product.product.productId}")
                    }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductSearchResult,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square image
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (product.product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.product.imageUrl,
                    contentDescription = product.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No Image", fontSize = 10.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.product.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = "RM ${String.format("%.2f", product.minPrice)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}