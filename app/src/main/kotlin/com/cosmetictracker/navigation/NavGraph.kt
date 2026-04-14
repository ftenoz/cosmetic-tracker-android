package com.cosmetictracker.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cosmetictracker.CosmeticTrackerApplication
import com.cosmetictracker.ui.auth.LoginScreen
import com.cosmetictracker.ui.auth.LoginViewModel
import com.cosmetictracker.ui.components.CTBottomNavigation
import com.cosmetictracker.ui.dashboard.DashboardScreen
import com.cosmetictracker.ui.products.ProductsViewModel
import com.cosmetictracker.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch

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

/**
 * Root composable that switches between Login and main app
 * based on token state. This is the correct architecture for
 * auth-aware navigation — no NavController-based auth redirects.
 */
@Composable
fun NavGraph(application: CosmeticTrackerApplication) {
    val loginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<LoginViewModel> {
        LoginViewModel(application.authRepository)
    }

    // Single source of truth for auth state
    val token by application.tokenManager.getToken().collectAsStateWithLifecycle(initialValue = "LOADING")

    Log.d("NavGraph", "Token state: ${when (token) { "LOADING" -> "LOADING"; null -> "NULL (not logged in)"; else -> "PRESENT (${token.take(10)}...)" }}")

    when {
        token == "LOADING" -> {
            // DataStore hasn't emitted yet — show brief loading indicator
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        token == null -> {
            // Logged out — show Login screen
            // resetState ensures stale Success state doesn't auto-redirect
            loginViewModel.resetState()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {},
                onLoginSuccess = {
                    // Token is already saved to DataStore by LoginViewModel.
                    // The token flow above will emit the new token and switch
                    // this composable to the main app automatically — no navController needed.
                }
            )
        }

        else -> {
            // Logged in — show main app with its own NavController
            // Using a separate navController here means it's always fresh on login
            MainApp(application = application)
        }
    }
}

@Composable
private fun MainApp(application: CosmeticTrackerApplication) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val productsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProductsViewModel> {
        ProductsViewModel(application.productRepository)
    }
    val profileViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProfileViewModel> {
        ProfileViewModel(application.authRepository)
    }

    val userNameState by application.tokenManager.getUserFirstName().collectAsStateWithLifecycle(initialValue = null)
    val userName = userNameState ?: "User"

    Scaffold(
        bottomBar = { CTBottomNavigation(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
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
                    onProductUpdated = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                com.cosmetictracker.ui.profile.ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        coroutineScope.launch {
                            application.authRepository.logout()
                            // Token becomes null → NavGraph switches to LoginScreen automatically
                            // No navController.navigate() needed!
                        }
                    }
                )
            }

            composable(Screen.Routines.route) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Routines Coming Soon", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
