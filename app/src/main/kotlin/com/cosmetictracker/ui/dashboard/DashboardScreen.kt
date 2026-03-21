package com.cosmetictracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmetictracker.data.model.ProductStats
import com.cosmetictracker.ui.products.ProductsUiState
import com.cosmetictracker.ui.products.ProductsViewModel
import com.cosmetictracker.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: ProductsViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProfile: () -> Unit,
    userName: String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load products when dashboard opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background) // f9f9ff
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp)) // Generous top spacing

        // Header - Editorial style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 0.01.sp
                    ),
                    color = OnSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        letterSpacing = (-0.02).sp
                    ),
                    color = OnBackground
                )
            }

            TextButton(onClick = onNavigateToProfile) {
                Text(
                    "Profile",
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Stats Cards
        when (val state = uiState) {
            is ProductsUiState.Success -> {
                StatsCards(stats = state.stats, onNavigateToProducts = onNavigateToProducts)
            }
            is ProductsUiState.Loading -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
            is ProductsUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Failed to load products",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnErrorContainer
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.loadProducts() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Add Product Button - Gradient pill
        Button(
            onClick = onNavigateToAddProduct,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50), // Pill shape
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = OnPrimary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Add Product",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatsCards(
    stats: ProductStats,
    onNavigateToProducts: () -> Unit
) {
    // Asymmetric layout - first card large, then two smaller
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Large card - Total
        StatCardLarge(
            title = "TOTAL PRODUCTS",
            value = stats.total.toString(),
            subtitle = "in your collection",
            onClick = onNavigateToProducts
        )

        // Two smaller cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCardCompact(
                title = "ACTIVE",
                value = stats.active.toString(),
                backgroundColor = TertiaryFixed,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToProducts
            )

            StatCardCompact(
                title = "EXPIRING",
                value = stats.expiringSoon.toString(),
                backgroundColor = PrimaryFixed,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToProducts
            )
        }
    }
}

@Composable
fun StatCardLarge(
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.05.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ),
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = (-0.02).sp
                ),
                color = Primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun StatCardCompact(
    title: String,
    value: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.05.sp
                ),
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = (-0.02).sp
                ),
                color = Primary
            )
        }
    }
}
