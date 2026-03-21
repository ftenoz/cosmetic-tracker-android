package com.cosmetictracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cosmetictracker.CosmeticTrackerApplication
import com.cosmetictracker.ui.auth.LoginScreen
import com.cosmetictracker.ui.auth.LoginViewModel
import com.cosmetictracker.ui.dashboard.DashboardScreen
import com.cosmetictracker.ui.products.ProductsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object AddProduct : Screen("add_product")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    application: CosmeticTrackerApplication
) {
    val loginViewModel = LoginViewModel(application.authRepository)
    val productsViewModel = ProductsViewModel(application.productRepository)

    // Check if logged in
    val isLoggedIn = runBlocking {
        application.tokenManager.getToken().first() != null
    }

    val userName = runBlocking {
        application.tokenManager.getUserFirstName().first() ?: "User"
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = { /* TODO */ },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = productsViewModel,
                onNavigateToProducts = { navController.navigate(Screen.Products.route) },
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                userName = userName
            )
        }

        composable(Screen.Products.route) {
            com.cosmetictracker.ui.products.ProductsScreen(
                viewModel = productsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToEditProduct = { /* TODO */ }
            )
        }

        composable(Screen.AddProduct.route) {
            com.cosmetictracker.ui.products.AddProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductAdded = {
                    navController.popBackStack()
                    productsViewModel.loadProducts()
                }
            )
        }

        composable(Screen.Profile.route) {
            com.cosmetictracker.ui.profile.ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    kotlinx.coroutines.runBlocking {
                        application.authRepository.logout()
                    }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
