package com.cosmetictracker.ui.products

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.cosmetictracker.data.model.Brand
import com.cosmetictracker.data.model.Category
import com.cosmetictracker.ui.components.BarcodeScanner
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTTextField
import com.cosmetictracker.ui.theme.SurfaceContainerLow
import com.cosmetictracker.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Product Details
    var productName by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var paoMonths by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val brands by viewModel.brands.collectAsStateWithLifecycle()
    var searchBrandQuery by remember { mutableStateOf("") }
    var brandExpanded by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Your Product Info
    var purchasedAt by remember { mutableStateOf<String?>(null) }
    var openedAt by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    // Image Handlers
    var obfImageUrl by remember { mutableStateOf<String?>(null) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    var isScanning by remember { mutableStateOf(false) }
    var saveLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isScanning = true
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_img_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            // We need a specific launcher for taking a photo. Let's define it.
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            localImageUri = cameraUri
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            localImageUri = uri
        }
    }

    if (isScanning) {
        BarcodeScanner(
            onBarcodeScanned = { scannedValue ->
                barcode = scannedValue
                isScanning = false
                
                coroutineScope.launch {
                    val product = viewModel.getProductDetailsFromBarcode(scannedValue)
                    if (product != null) {
                        if (!product.product_name.isNullOrBlank()) productName = product.product_name
                        if (!product.brands.isNullOrBlank()) {
                            val parsedBrand = product.brands.split(",").firstOrNull()?.trim() ?: product.brands
                            searchBrandQuery = parsedBrand
                        }
                        if (!product.image_front_url.isNullOrBlank()) obfImageUrl = product.image_front_url
                        android.widget.Toast.makeText(context, "Dış servis bilgisi getirildi!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onCancel = { isScanning = false }
        )
    } else {
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // =============== PRODUCT DETAILS ===============
                Text(
                    text = "Product Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                CTTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = "Product Name *",
                    placeholder = "e.g., Pro Filt'r Foundation"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Brand Dropdown (Selectable & Editable)
                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = searchBrandQuery,
                            onValueChange = { searchBrandQuery = it },
                            label = { Text("Brand *") },
                            placeholder = { Text("Search or enter brand") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SurfaceContainerLow,
                                focusedContainerColor = SurfaceContainerLowest
                            ),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) }
                        )
                        val filteredBrands = brands.filter { it.name.contains(searchBrandQuery, ignoreCase = true) }
                        if (filteredBrands.isNotEmpty() || searchBrandQuery.isNotBlank()) {
                            ExposedDropdownMenu(
                                expanded = brandExpanded,
                                onDismissRequest = { brandExpanded = false }
                            ) {
                                filteredBrands.take(5).forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b.name) },
                                        onClick = {
                                            searchBrandQuery = b.name
                                            brandExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        val currentCategoryText = selectedCategory?.name ?: if (categories.isNotEmpty()) {
                            selectedCategory = categories.first()
                            categories.first().name
                        } else "Loading..."

                        OutlinedTextField(
                            value = currentCategoryText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SurfaceContainerLow,
                                focusedContainerColor = SurfaceContainerLowest
                            ),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CTTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = "Barcode",
                        placeholder = "Optional",
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    isScanning = true
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Scan")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = paoMonths,
                        onValueChange = { paoMonths = it.filter { char -> char.isDigit() } },
                        label = { Text("PAO") },
                        placeholder = { Text("12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Text("months", modifier = Modifier.padding(end = 16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedContainerColor = SurfaceContainerLowest
                        )
                    )
                }

                CTTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Optional details about the product",
                    singleLine = false,
                    maxLines = 4
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // =============== YOUR PRODUCT INFO ===============
                Text(
                    text = "Your Product Info",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Product Image", style = MaterialTheme.typography.labelMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageToLoad = localImageUri ?: obfImageUrl
                    if (imageToLoad != null) {
                        AsyncImage(
                            model = imageToLoad,
                            contentDescription = "Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = SurfaceContainerLow) {}
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val file = File(context.cacheDir, "camera_img_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                cameraUri = uri
                                takePictureLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    var showPurchasedPicker by remember { mutableStateOf(false) }
                    val purchasedPickerState = rememberDatePickerState()
                    Box(modifier = Modifier.weight(1f).clickable { showPurchasedPicker = true }) {
                        OutlinedTextField(
                            value = purchasedAt ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false, // Prevents typing but allows Box clicking
                            label = { Text("Purchase Date") },
                            placeholder = { Text("dd.mm.yyyy") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                    if (showPurchasedPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showPurchasedPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    purchasedPickerState.selectedDateMillis?.let { millis ->
                                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                        purchasedAt = date.toString()
                                    }
                                    showPurchasedPicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = { TextButton(onClick = { showPurchasedPicker = false }) { Text("Cancel") } }
                        ) { DatePicker(state = purchasedPickerState) }
                    }

                    var showOpenedPicker by remember { mutableStateOf(false) }
                    val openedPickerState = rememberDatePickerState()
                    Box(modifier = Modifier.weight(1f).clickable { showOpenedPicker = true }) {
                        OutlinedTextField(
                            value = openedAt ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Opened Date") },
                            placeholder = { Text("dd.mm.yyyy") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                    if (showOpenedPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showOpenedPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    openedPickerState.selectedDateMillis?.let { millis ->
                                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                        openedAt = date.toString()
                                    }
                                    showOpenedPicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = { TextButton(onClick = { showOpenedPicker = false }) { Text("Cancel") } }
                        ) { DatePicker(state = openedPickerState) }
                    }
                }

                CTTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    placeholder = "Personal notes, shade, etc.",
                    singleLine = false,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                CTButton(
                    text = if (saveLoading) "Saving..." else "Add Product",
                    onClick = {
                        if (productName.isBlank() || searchBrandQuery.isBlank()) {
                            android.widget.Toast.makeText(context, "Please fill in Product Name and Brand.", android.widget.Toast.LENGTH_SHORT).show()
                            return@CTButton
                        }
                        saveLoading = true
                        var imageFile: File? = null
                        if (localImageUri != null) {
                            val inputStream = context.contentResolver.openInputStream(localImageUri!!)
                            if (inputStream != null) {
                                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
                                tempFile.outputStream().use { output ->
                                    inputStream.copyTo(output)
                                }
                                imageFile = tempFile
                            }
                        }
                        
                        val chosenCatId = selectedCategory?.id ?: categories.firstOrNull()?.id ?: ""

                        viewModel.addProduct(
                            name = productName,
                            brandName = searchBrandQuery,
                            categoryId = chosenCatId,
                            barcode = barcode.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                            description = description.takeIf { it.isNotBlank() },
                            paoMonths = paoMonths.toIntOrNull(),
                            purchasedAt = purchasedAt,
                            openedAt = openedAt,
                            imageFile = imageFile,
                            obfImageUrl = if (localImageUri == null) obfImageUrl else null,
                            onComplete = { success, msg ->
                                saveLoading = false
                                if (success) {
                                    onProductAdded()
                                } else {
                                    android.widget.Toast.makeText(context, msg ?: "Error saving", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    },
                    enabled = !saveLoading
                )

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
