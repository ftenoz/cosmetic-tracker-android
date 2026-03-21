package com.cosmetictracker.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Product Information",
                style = MaterialTheme.typography.titleLarge
            )

            CTTextField(
                value = productName,
                onValueChange = { productName = it },
                label = "Product Name",
                placeholder = "e.g., Fenty Beauty Foundation"
            )

            CTTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "Brand",
                placeholder = "e.g., Fenty Beauty"
            )

            CTTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = "Barcode (optional)",
                placeholder = "Scan or enter manually"
            )

            CTTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                placeholder = "Shade, usage notes...",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            CTButton(
                text = "Add Product",
                onClick = {
                    // TODO: Save product
                    onProductAdded()
                },
                enabled = productName.isNotBlank() && brand.isNotBlank()
            )

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
