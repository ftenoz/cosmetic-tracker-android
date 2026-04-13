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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTTextField

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit,
    onProductUpdated: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val product = remember(productId) { viewModel.getProductById(productId) }

    if (product == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Product") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Product not found.", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    // States initialized with existing product data
    var purchasedAt by remember { mutableStateOf(product.purchasedAt) }
    var openedAt by remember { mutableStateOf(product.openedAt) }
    var notes by remember { mutableStateOf(product.notes ?: "") }

    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    
    var saveLoading by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_img_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
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
            
            // Read-Only Product Info Header
            Text(
                text = "Product Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            OutlinedTextField(
                value = product.product?.brand?.name ?: "Unknown",
                onValueChange = {},
                readOnly = true,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = product.product?.name ?: "Unknown",
                onValueChange = {},
                readOnly = true,
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Editable Info
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
                val imageToLoad = localImageUri ?: product.imageUrl
                if (imageToLoad != null) {
                    AsyncImage(
                        model = imageToLoad,
                        contentDescription = "Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerHighest) {}
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
                
                // Purchase Date
                var showPurchasedPicker by remember { mutableStateOf(false) }
                val initialPurchasedMillis = try { 
                    purchasedAt?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() } 
                } catch(e: Exception) { null }
                val purchasedPickerState = rememberDatePickerState(initialSelectedDateMillis = initialPurchasedMillis)
                
                Box(modifier = Modifier.weight(1f).clickable { showPurchasedPicker = true }) {
                    OutlinedTextField(
                        value = purchasedAt ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
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

                // Opened Date
                var showOpenedPicker by remember { mutableStateOf(false) }
                val initialOpenedMillis = try { 
                    openedAt?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() } 
                } catch(e: Exception) { null }
                val openedPickerState = rememberDatePickerState(initialSelectedDateMillis = initialOpenedMillis)
                
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
                placeholder = "Personal notes...",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            CTButton(
                text = if (saveLoading) "Saving..." else "Save Changes",
                onClick = {
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
                    
                    viewModel.updateProduct(
                        id = productId,
                        purchasedAt = purchasedAt,
                        openedAt = openedAt,
                        notes = notes.takeIf { it.isNotBlank() },
                        imageFile = imageFile,
                        existingImageUrl = product.imageUrl,
                        onComplete = { success, msg ->
                            saveLoading = false
                            if (success) {
                                onProductUpdated()
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
