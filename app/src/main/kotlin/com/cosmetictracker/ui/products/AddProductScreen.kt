package com.cosmetictracker.ui.products

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.cosmetictracker.data.model.Category
import com.cosmetictracker.ui.components.BarcodeScanner
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTTextField
import com.cosmetictracker.ui.theme.SurfaceContainerLow
import com.cosmetictracker.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var productName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var obfImageUrl by remember { mutableStateOf<String?>(null) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    var isScanning by remember { mutableStateOf(false) }
    var saveLoading by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isScanning = true
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
                        if (!product.product_name.isNullOrBlank()) {
                            productName = product.product_name
                        }
                        if (!product.brands.isNullOrBlank()) {
                            brand = product.brands.split(",").firstOrNull()?.trim() ?: product.brands
                        }
                        if (!product.image_front_url.isNullOrBlank()) {
                            obfImageUrl = product.image_front_url
                        }
                        android.widget.Toast.makeText(context, "Ürün bilgileri getirildi!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Veritabanında bulunamadı.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onCancel = {
                isScanning = false
            }
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
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageToLoad = localImageUri ?: obfImageUrl
                    if (imageToLoad != null) {
                        AsyncImage(
                            model = imageToLoad,
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)) {
                                Icon(Icons.Default.Image, contentDescription = "Edit Image", modifier = Modifier.padding(8.dp))
                            }
                        }
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = SurfaceContainerLow) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Image, contentDescription = "Add Image", modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Fotoğraf Ekle (İsteğe Bağlı)")
                            }
                        }
                    }
                }

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

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    val currentCategoryText = selectedCategory?.name ?: if (categories.isNotEmpty()) {
                        selectedCategory = categories.first()
                        categories.first().name
                    } else "Loading categories..."

                    OutlinedTextField(
                        value = currentCategoryText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
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

                CTTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = "Barcode (optional)",
                    placeholder = "Scan or enter manually",
                    trailingIcon = {
                        IconButton(onClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                isScanning = true
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan Barcode"
                            )
                        }
                    }
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
                    text = if (saveLoading) "Saving..." else "Add Product",
                    onClick = {
                        if (productName.isBlank() || brand.isBlank()) {
                            android.widget.Toast.makeText(context, "Lütfen ürün adı ve marka giriniz.", android.widget.Toast.LENGTH_SHORT).show()
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
                            brandName = brand,
                            categoryId = chosenCatId,
                            barcode = barcode.takeIf { it.isNotBlank() },
                            notes = notes,
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
