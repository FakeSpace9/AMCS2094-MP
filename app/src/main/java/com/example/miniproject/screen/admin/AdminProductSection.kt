package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.LoginViewModel
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel
import com.example.miniproject.viewmodel.PromotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductSection(
    navController: NavController,
    formViewModel: ProductFormViewModel,
    searchViewModel: ProductSearchViewModel,
    loginViewModel: LoginViewModel,
    promoViewModel: PromotionViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Search", "Entry", "Edit", "Promo")

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F9FA))) {

        // --- IMPROVED TOP BAR ---
        CenterAlignedTopAppBar(
            title = {
                Text("Manage Products", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            actions = {
                IconButton(onClick = { navController.navigate("admin_profile") }) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color(0xFF573BFF),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF8F9FA))
        )

        // --- CUSTOM TAB SELECTOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(25.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    val isEditTab = index == 2
                    val canClick = !isEditTab

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(
                                if (selected) Color(0xFF573BFF).copy(alpha = 0.1f) else Color.Transparent
                            )
                            .clickable(enabled = canClick) {
                                selectedTab = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selected) Color(0xFF573BFF) else if (isEditTab) Color.LightGray else Color.Gray
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            when (selectedTab) {
                0 -> AdminSearchScreen(
                    viewModel = searchViewModel,
                    onProductClick = { product ->
                        formViewModel.loadProductForEdit(product)
                        selectedTab = 2
                    }
                )
                1 -> {
                    LaunchedEffect(Unit) { formViewModel.resetState() }
                    AdminAddProductForm(navController, formViewModel)
                }
                2 -> {
                    AdminEditProductForm(
                        navController,
                        formViewModel,
                        onUpdateSuccess = {
                            selectedTab = 0
                            searchViewModel.loadProducts()
                        }
                    )
                }
                3 -> {
                    AdminPromotionScreen(viewModel = promoViewModel)
                }
            }
        }
    }
}