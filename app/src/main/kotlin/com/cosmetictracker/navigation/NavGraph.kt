package com.cosmetictracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cosmetictracker.CosmeticTrackerApplication
import com.cosmetictracker.ui.auth.LoginScreen
import com.cosmetictracker.ui.auth.LoginViewModel
import com.cosmetictracker.ui.dashboard.DashboardScreen
import com.cosmetictracker.ui.products.ProductsViewModel
import com.cosmetictracker.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import com.cosmetictracker.ui.components.CTBottomNavigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object AddProduct : Screen("add_product")
    object Routines : Screen("routines")
    object Profile : Screen("profile")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    application: CosmeticTrackerApplication
) {
    // Remember ViewModels to survive recomposition
    val loginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<LoginViewModel> {
        LoginViewModel(application.authRepository)
    }
    val productsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProductsViewModel> {
        ProductsViewModel(application.productRepository)
    }
    val profileViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProfileViewModel> {
        ProfileViewModel(application.authRepository)
    }

    // Check if logged in
    val isLoggedIn = runBlocking {
        application.tokenManager.getToken().first() != null
    }

    val userNameState by application.tokenManager.getUserFirstName().collectAsStateWithLifecycle(initialValue = null)
    val userName = userNameState ?: "User"

    val coroutineScope = rememberCoroutineScope()

    // Hide bottom nav on login screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CTBottomNavigation(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
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
                userName = userName
            )
        }

        composable(Screen.Products.route) {
            com.cosmetictracker.ui.products.ProductsScreen(
                viewModel = productsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToEditProduct = { id -> navController.navigate(Screen.EditProduct.createRoute(id)) },
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
            )
        }

        composable(Screen.AddProduct.route) {
            com.cosmetictracker.ui.products.AddProductScreen(
                viewModel = productsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onProductAdded = {
                    navController.popBackStack()
                    productsViewModel.loadProducts()
                }
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            com.cosmetictracker.ui.products.ProductDetailScreen(
                productId = productId,
                viewModel = productsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditProduct.createRoute(id)) }
            )
        }

        composable(Screen.EditProduct.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            com.cosmetictracker.ui.products.EditProductScreen(
                productId = productId,
                viewModel = productsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onProductUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            com.cosmetictracker.ui.profile.ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    coroutineScope.launch {
                        application.authRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Routines.route) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Routines Coming Soon", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
    }
}
