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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cosmetictracker.data.model.ExpiryStatus
import com.cosmetictracker.data.model.UserProduct
import com.cosmetictracker.ui.theme.*
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
                title = { Text("Product Details") },
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
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Product not found.", color = MaterialTheme.colorScheme.error)
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
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top Image Section
        if (product.imageUrl != null) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.product?.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLowest),
                contentAlignment = Alignment.Center
            ) {
                Text("💄", style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp))
            }
        }

        // Header Section (Brand + Product Name)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = product.product?.brand?.name ?: "Unknown Brand",
                style = MaterialTheme.typography.titleMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.product?.name ?: "Unknown Product",
                style = MaterialTheme.typography.headlineMedium,
                color = OnBackground,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Status Badge
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            StatusBadge(status = expiryStatus)
        }

        // Expiry Tracker Card
        ExpiryTrackerCard(product = product)

        // Details Information Card
        DetailsCard(product = product)

        // Notes Card
        if (!product.notes.isNullOrBlank()) {
            NotesCard(notes = product.notes)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ExpiryTrackerCard(product: UserProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Lifecycle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                            fontWeight = FontWeight.Bold,
                            color = if (remainingMonths <= 1) Error else Primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) Error else if (progress >= 0.8f) PrimaryFixed else Primary,
                        trackColor = SurfaceContainerHighest
                    )
                } else {
                    Text("Invalid Open Date format.", color = Error)
                }
            } else {
                Text(
                    "Not opened yet. Product lifecycle begins when marked as Opened.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailsCard(product: UserProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
             Text(
                text = "Product Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
            DetailRow(label = "PAO (Period After Opening)", value = "${product.product?.paoMonths ?: "Unknown"} Months")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
    }
}

@Composable
fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                color = OnSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSecondaryContainer
            )
        }
    }
}
