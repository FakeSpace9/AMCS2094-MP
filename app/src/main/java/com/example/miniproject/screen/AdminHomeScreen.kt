package com.example.miniproject.screen

/*
@Composable
fun AdminHomeScreen(
    userViewModel: UserViewModel,
    loginViewModel: LoginViewModel,
    navController: NavController
) {

    val currentUser by userViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUser()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()

            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, Admin!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Welcome, ${currentUser?.name ?: "Admin"}!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "logout", modifier = Modifier.clickable {
            loginViewModel.logout()
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        })
    }

}

 */