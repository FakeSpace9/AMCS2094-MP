package com.example.miniproject.screen.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.data.entity.CartEntity
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel
){
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedIds by viewModel.selectedItemIds.collectAsState()
    val selectedTotal by viewModel.selectedTotal.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("My Cart",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }){
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if(cartItems.isNotEmpty()){
                CheckoutBottomBar(
                    total = selectedTotal,
                    enabled = selectedIds.isNotEmpty(),
                    onCheckoutClick = {
                        // Navigate to checkout with selected IDs
                        val idsString = selectedIds.joinToString(",")
                        navController.navigate("checkout?itemIds=$idsString")
                    }
                )
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ){ paddingValues ->
        if(cartItems.isEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "Your cart is empty",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Select All Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedIds.size == cartItems.size && cartItems.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            if (isChecked) viewModel.selectAll() else viewModel.deselectAll()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = PurpleAccent)
                    )
                    Text("Select All", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    items(cartItems){ item ->
                        CartItemCard(
                            item = item,
                            isSelected = selectedIds.contains(item.id),
                            onSelectionChange = { viewModel.toggleSelection(item.id) },
                            onQuantityChange = { newQty ->
                                viewModel.updateQuantity(item, newQty)
                            },
                            onRemoveClick = {
                                viewModel.removeFromCart(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartEntity,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                colors = CheckboxDefaults.colors(checkedColor = PurpleAccent)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.productImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    Column(modifier = Modifier.weight(1f)){
                        Text(
                            text = item.productName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.selectedColour} | ${item.selectedSize}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable{ onRemoveClick() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "RM${String.format("%.2f",item.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PurpleAccent
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6),
                                RoundedCornerShape(8.dp))
                    ){
                        IconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            modifier = Modifier.size(32.dp)
                        ){
                            Icon(Icons.Default.Remove, null, tint = Color.Gray,modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = item.quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        IconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        ){
                            Icon(Icons.Default.Add, null, tint = Color.Gray,modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutBottomBar(
    total: Double,
    enabled: Boolean,
    onCheckoutClick: () -> Unit
){
    Surface(
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Button(
            onClick = onCheckoutClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleAccent,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "CHECKOUT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "RM${String.format("%.2f",total)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                }
            }
        }
    }
}