package com.example.miniproject.screen.customer

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miniproject.screen.TopBar
import com.example.miniproject.ui.theme.PurpleAccent
import com.example.miniproject.viewmodel.AddToCartStatus
import com.example.miniproject.viewmodel.ProductDetailScreenViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    viewModel: ProductDetailScreenViewModel
){
    val product by viewModel.product.collectAsState()
    val availableSizes by viewModel.availableSizes.collectAsState()
    val selectedSizes by viewModel.selectedSize.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val selectedVariant by viewModel.selectedVariant.collectAsState()
    val addToCartStatus by viewModel.addToCartStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(addToCartStatus) {
        when (addToCartStatus) {
            is AddToCartStatus.Success -> {
                Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartStatus() // Reset so we can add again


            }
            is AddToCartStatus.Error -> {
                Toast.makeText(context, (addToCartStatus as AddToCartStatus.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartStatus()

            }
            else -> {}
        }
    }

    LaunchedEffect(productId)
    {
        viewModel.loadProductData(productId)
    }
    Scaffold(
        topBar ={
            TopAppBar(
             title = {},
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint  = Color.Black
                        )
                    }
                },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.addToCart() },
                enabled = addToCartStatus !is AddToCartStatus.Loading &&(selectedVariant?.stockQuantity ?:0)>0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(12.dp)
            ){
                if(addToCartStatus is AddToCartStatus.Loading){
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }else{
                    val buttonText = if((selectedVariant?.stockQuantity ?: 0)<= 0 && selectedSizes.isNotEmpty())"Out Of Stock" else "Add to cart"
                    Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

            }
        },
                    containerColor = Color.White
    ) {
        paddingValues ->
                    if(product == null){
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ){
                            CircularProgressIndicator(color = PurpleAccent)
                        }
                    }else{
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9FAFB)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(product!!.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = product!!.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
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
                                    modifier = Modifier.clickable { /*TODO*/ }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)){
                                items(availableSizes){size->
                                    SizeCard(
                                        sizeText = size,
                                        isSelected = size == selectedSizes,
                                        onClick = { viewModel.selectSize(size) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))

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
                                    .background(Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ){
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
fun SizeCard(
    sizeText:String,
    isSelected: Boolean,
    onClick: () -> Unit
){
    val backgroundColor = if(isSelected) PurpleAccent else Color.White
    val textColor = if(isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable{onClick()},
        contentAlignment = Alignment.Center
    ){
        Text(
            text = sizeText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

