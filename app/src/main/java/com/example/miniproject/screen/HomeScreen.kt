package com.example.miniproject.screen

import androidx.compose.foundation.Image
import com.example.miniproject.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.data.dao.ProductSearchResult
import com.example.miniproject.viewmodel.LoginStateCustomer
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.PromotionViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreenWithDrawer(navController: NavController,
                         viewModel: LoginViewModel,
                         searchViewModel: ProductSearchViewModel,
                         promoViewModel: PromotionViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.5f), // half transparent background
        drawerContent = {
            DrawerMenu(navController = navController, viewModel = viewModel)
        }
    ) {
        HomeScreen(
            navController = navController,
            onMenuClick = {
                scope.launch { drawerState.open() }
            },
            loginViewModel = viewModel,
            searchViewModel = searchViewModel,
            promoViewModel = promoViewModel
        )
    }
}

@Composable
fun HomeScreen(navController: NavController,
               onMenuClick: () -> Unit,
               loginViewModel: LoginViewModel,
               searchViewModel: ProductSearchViewModel,
               promoViewModel: PromotionViewModel
) {
    val customerLoginState by loginViewModel.customerState.collectAsState()
    val products by searchViewModel.searchResults.collectAsState()

    LaunchedEffect(Unit) {
        promoViewModel.syncPromotions()
        searchViewModel.loadProducts() // Ensure products are loaded
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item { TopBar(onMenuClick, viewModel = loginViewModel, navController = navController) }
        item { MenuTabs( navController = navController, searchViewModel = searchViewModel) }
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

        item {
            if (products.isNotEmpty()) {
                val bestSellers = products.take(3)
                ProductSection(
                    title = "Best Seller",
                    products = bestSellers,
                    navController = navController
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Category Products", // Or "Just For You"
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Create a Grid manually inside LazyColumn using chunked
        val categoryProducts = if (products.size > 3) products.drop(3) else emptyList()
        // If you want to show ALL products in category section regardless of Best Seller, remove .drop(3)

        items(categoryProducts.chunked(3)) { rowProducts ->
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
                            navController.navigate("productDetail/${product.product.productId}")
                        }
                    )
                }
                // If row has only 1 item, add a spacer to align it correctly
                val remainingSlots = 3 - rowProducts.size
                if (remainingSlots > 0) {
                    repeat(remainingSlots) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Bottom Spacer
        item { Spacer(modifier = Modifier.height(20.dp)) }

    }
}

@Composable
fun DrawerMenu(navController: NavController, viewModel: LoginViewModel) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Menu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))

        Text("New Arrivals")
        Spacer(Modifier.height(12.dp))
        Text("Categories")
        Spacer(Modifier.height(12.dp))
        Text("Sales")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun TopBar(onMenuClick: () -> Unit, viewModel: LoginViewModel, navController: NavController) {
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
            Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.clickable { onMenuClick() })
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.Search, contentDescription = "Search")
        Spacer(modifier = Modifier.weight(1f))
        Text("Shop Name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart",modifier = Modifier.clickable {
            if (isLoggedIn) {
                navController.navigate("cart")
            } else {
                navController.navigate("Login")
            }
        })
        Spacer(modifier = Modifier.width(12.dp))

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
    }
}

@Composable
fun MenuTabs(
    navController: NavController,
    searchViewModel: ProductSearchViewModel
) {
    var showCategories by remember { mutableStateOf(false) }
    val categories = searchViewModel.getAvailableCategories()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "New Arrivals",
            modifier = Modifier.clickable {
                searchViewModel.selectedCategory.value = "New Arrivals"
                navController.navigate("menu")
            },
            fontWeight = FontWeight.Medium
        )
        Box{
        Text(
            text = "Categories ▼",
            modifier = Modifier.clickable {
                showCategories = true
            },
            fontWeight = FontWeight.Medium
        )
        DropdownMenu(
            expanded = showCategories,
            onDismissRequest = { showCategories = false },
            modifier = Modifier.background(Color.White)
        ) {
            categories.filter { it != "All" }.forEach { categories ->
                DropdownMenuItem(
                    text = { Text(categories) },
                    onClick = {
                        searchViewModel.selectedCategory.value = categories
                        navController.navigate("menu")
                        showCategories = false
                    }
                )
            }
            }
        }
    }
}

@Composable
fun SalesBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFECECEC)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.promotionbanner), // Change this ID if your file name is different
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
        // Placeholder Icon for Category
        Icon(
            imageVector = Icons.Outlined.Category, // You can swap this based on category name
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
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
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