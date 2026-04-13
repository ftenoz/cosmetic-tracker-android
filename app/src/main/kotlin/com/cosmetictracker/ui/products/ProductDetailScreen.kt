package com.cosmetictracker.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cosmetictracker.data.model.ExpiryStatus
import com.cosmetictracker.data.model.UserProduct
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val product = remember(productId) { viewModel.getProductById(productId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (product != null) {
                        IconButton(onClick = { onNavigateToEdit(productId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Item not found.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val expiryStatus = viewModel.getExpiryStatus(product)
            ProductDetailContent(
                product = product,
                expiryStatus = expiryStatus,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun ProductDetailContent(
    product: UserProduct,
    expiryStatus: ExpiryStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Image Layer
        if (product.imageUrl != null) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.product?.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", style = MaterialTheme.typography.displayLarge)
            }
        }

        // Editorial Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = product.product?.brand?.name ?: "Unknown Brand",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.product?.name ?: "Unknown Product",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        StatusBadge(status = expiryStatus)

        ExpiryTrackerCard(product = product)

        DetailsCard(product = product)

        if (!product.notes.isNullOrBlank()) {
            NotesCard(notes = product.notes)
        }
        
        Spacer(modifier = Modifier.height(56.dp))
    }
}

@Composable
fun ExpiryTrackerCard(product: UserProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(
                text = "LIFECYCLE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            val openedAt = product.openedAt
            val paoMonths = product.product?.paoMonths

            if (openedAt != null && paoMonths != null && paoMonths > 0) {
                val openedDate = try {
                    LocalDate.parse(openedAt, DateTimeFormatter.ISO_DATE)
                } catch (e: Exception) { null }

                if (openedDate != null) {
                    val today = LocalDate.now()
                    val monthsOpen = ChronoUnit.MONTHS.between(openedDate, today).toInt()
                    val progress = (monthsOpen.toFloat() / paoMonths.toFloat()).coerceIn(0f, 1f)
                    
                    val remainingMonths = paoMonths - monthsOpen

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Opened: $openedAt", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (remainingMonths > 0) "$remainingMonths mos left" else "Expired",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (remainingMonths <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(percent = 50)),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                } else {
                    Text("Invalid Open Date format.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    "Not opened yet. Lifecycle begins when marked as Opened.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailsCard(product: UserProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
             Text(
                text = "FACTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            DetailRow(label = "Category", value = product.product?.category?.name ?: "Unknown")
            if (!product.product?.barcode.isNullOrBlank()) {
                DetailRow(label = "Barcode", value = product.product?.barcode ?: "")
            }
            if (product.purchasedAt != null) {
                DetailRow(label = "Purchased", value = product.purchasedAt ?: "")
            }
            if (!product.product?.description.isNullOrBlank()) {
                DetailRow(label = "Description", value = product.product?.description ?: "")
            }
            DetailRow(label = "PAO (Period After Opening)", value = "${product.product?.paoMonths ?: "0"} Months")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(
                text = "YOUR NOTES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
