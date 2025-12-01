package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.ProductFormViewModel
import com.example.miniproject.viewmodel.ProductSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductSection(
    navController: NavController,
    formViewModel: ProductFormViewModel,
    searchViewModel: ProductSearchViewModel
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to "Entry"
    val tabs = listOf("Search", "Entry", "Edit")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Top Bar
        TopAppBar(
            title = { Text("Product", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
            actions = {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.padding(end = 16.dp).size(32.dp),
                    tint = Color.Gray
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
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
                                if (selected) Color(0xFFF0F0F0) else Color.Transparent
                            )
                            .clickable(enabled = canClick) {
                                selectedTab = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            color = if (isEditTab && !selected) Color.LightGray else Color.Black
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            when (selectedTab) {
                0 -> AdminSearchScreen(
                    viewModel = searchViewModel, // Using Search VM
                    onProductClick = { product ->
                        // 1. Load data into Form VM
                        formViewModel.loadProductForEdit(product)
                        // 2. Switch to Edit Tab
                        selectedTab = 2
                    }
                )
                1 -> {
                    // Reset Form VM
                    LaunchedEffect(Unit) { formViewModel.resetState() }
                    AdminAddProductForm(navController, formViewModel)
                }
                2 -> {
                    // Edit Form VM
                    AdminEditProductForm(navController, formViewModel)
                }
            }
        }
    }
}