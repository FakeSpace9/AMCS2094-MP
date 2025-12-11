package com.example.miniproject.screen.customer

import android.R.attr.singleLine
import android.system.Os.remove
import android.view.RoundedCorner
import androidx.annotation.experimental.Experimental
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.util.TableInfo
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
    val subtotal by viewModel.subtotal.collectAsState()
    val total by viewModel.total.collectAsState()
    val promoCode by viewModel.promoCode.collectAsState()
    val promoCodeError by viewModel.promoCodeError.collectAsState()

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
                        CheckoutBottomBar(total = total, onCheckoutClick = { navController.navigate("checkout") })
                    }
                },
                containerColor = Color(0xFFF9FAFB)
            ){
                paddingValues ->
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
                }else{
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ){
                        items(cartItems){item->
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQty ->
                                    viewModel.updateQuantity(item, newQty)
                                },
                                onRemoveClick = {
                                    viewModel.removeFromCart(item)
                                }
                            )
                        }
                        item{
                            Spacer(modifier = Modifier.height(8.dp))
                            PromoCodeSection(
                                promoCode = promoCode,
                                onPromoCodeChange = { viewModel.onPromoCodeChange(it) },
                                onApplyClick = { viewModel.applyVoucherCode() },
                                error = promoCodeError

                            )
                        }
                        item{
                            Spacer(modifier = Modifier.height(8.dp))
                            PriceBreakDownSection(
                                subtotal = subtotal,
                                shippingFee = viewModel.shippingFee,
                            )
                        }
                    }
                }
    }
}

@Composable
fun CartItemCard(
    item: CartEntity,
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCodeSection(
    promoCode: String,
    onPromoCodeChange:(String)-> Unit,
    onApplyClick:()-> Unit,
    error: String?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = promoCode,
                onValueChange = onPromoCodeChange,
                placeholder = {Text("Enter promo code", color = Color.Gray)},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurpleAccent,
                    errorTextColor = Color.Red
                    ),
                isError = error != null,
                singleLine = true
            )
            Button(
                onClick = onApplyClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ){
                Text(
                    text = "APPLY",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if(error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp,top=4.dp)
            )
        }
    }
}

@Composable
fun PriceBreakDownSection(subtotal: Double, shippingFee: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Text(
                text = "Subtotal",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                "RM${String.format("%.2f",subtotal)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "Shipping Fee",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                "RM${String.format("%.2f",shippingFee)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CheckoutBottomBar(
    total: Double,
    onCheckoutClick: () -> Unit
){
    Surface(
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Button(
            onClick = onCheckoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
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