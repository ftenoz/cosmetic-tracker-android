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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome back,\n$userName!",
                style = MaterialTheme.typography.headlineMedium,
                color = OnBackground
            )

            TextButton(onClick = onNavigateToProfile) {
                Text("Profile")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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

        // Add Product Button
        Button(
            onClick = onNavigateToAddProduct,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Product", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StatsCards(
    stats: ProductStats,
    onNavigateToProducts: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(
            title = "Total Products",
            value = stats.total.toString(),
            emoji = "💄",
            backgroundColor = SurfaceContainerLowest,
            onClick = onNavigateToProducts
        )

        StatCard(
            title = "Active",
            value = stats.active.toString(),
            emoji = "✨",
            backgroundColor = TertiaryFixed,
            onClick = onNavigateToProducts
        )

        StatCard(
            title = "Expiring Soon",
            value = stats.expiringSoon.toString(),
            emoji = "⚠️",
            backgroundColor = PrimaryFixed,
            onClick = onNavigateToProducts
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    emoji: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = Primary
                )
            }
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
