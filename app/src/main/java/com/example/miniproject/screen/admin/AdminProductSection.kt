package com.example.miniproject.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.viewmodel.AddProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductSection(
    navController: NavController,
    addProductViewModel: AddProductViewModel
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
                    modifier = Modifier.padding(end = 16.dp).padding(8.dp),
                    tint = Color.Gray
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFF8F9FA),
            contentColor = Color.Black,
            divider = {}, // Remove divider
            indicator = {} // Remove default indicator
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                val isEditTab = index == 2 // Check if this is the "Edit" tab

                Tab(
                    selected = selected,
                    onClick = {
                        // FIXED: Only allow clicking if it is NOT the Edit tab
                        if (!isEditTab) {
                            selectedTab = index
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Pill Shape Background
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) Color(0xFFE9E9E9) else Color.Transparent
                            )
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            // Visual Hint: Grey out the text if it's the Edit tab and not selected
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
                    viewModel = addProductViewModel,
                    onProductClick = { product ->
                        // 1. Load data into ViewModel
                        addProductViewModel.loadProductForEdit(product)
                        // 2. Switch to Edit Tab PROGRAMMATICALLY
                        selectedTab = 2
                    }
                )
                1 -> {
                    // Reset to "Add Mode" when entering this tab
                    LaunchedEffect(Unit) { addProductViewModel.resetState() }
                    AdminAddProductForm(navController, addProductViewModel)
                }
                2 -> {
                    // Edit Form (Now separated)
                    AdminEditProductForm(navController, addProductViewModel)
                }
            }
        }
    }
}