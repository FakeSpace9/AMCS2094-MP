
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.viewmodel.LoginStateCustomer

import com.example.miniproject.viewmodel.LoginViewModel


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel) {
    val customerLoginState by viewModel.customerState.collectAsState()
    val context = LocalContext.current

    // States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        // SHOP NAME
        Text(
            text = "SHOP NAME",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Sign In Title
        Text(
            text = "Sign in",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))
        LaunchedEffect(customerLoginState) {
            when (customerLoginState) {
                is LoginStateCustomer.Success -> {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                    navController.navigate("home") {
                        popUpTo("Login") { inclusive = true }
                    }
                }
                is LoginStateCustomer.Error -> {
                    Toast.makeText(
                        context,
                        (customerLoginState as LoginStateCustomer.Error).message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> { /* do nothing for Idle or Loading */ }
            }
        }


        // Continue Button
        Button(
            onClick = {
                if (email == "admin" && password == "admin") {
                    navController.navigate("admin_signup")
                } else {
                    viewModel.login(email, password)

                }
            },
            enabled = customerLoginState !is LoginStateCustomer.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFECECEC)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (customerLoginState is LoginStateCustomer.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 3.dp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                // Trigger Google Sign-In
                viewModel.signInWithGoogle(context as ComponentActivity)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(3.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google__g__logo_svg),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(30.dp)   // make image balanced
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Sign in with Google",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

        }





        Spacer(modifier = Modifier.height(20.dp))

        // OR Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                "   or   ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign Up Button
        Button(
            onClick = { navController.navigate("Signup") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF573BFF) // Purple
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Sign Up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Forgot Password
        Text(
            text = "Forgot Password",
            fontSize = 16.sp,
            modifier = Modifier.clickable {
                navController.navigate("forgot_password")
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Forgot Password
        Text(
            text = "Staff Login",
            fontSize = 16.sp,
            modifier = Modifier.clickable {
                navController.navigate("adminLogin")
            }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}
