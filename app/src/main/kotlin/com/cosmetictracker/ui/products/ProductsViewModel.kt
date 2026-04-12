package com.cosmetictracker.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmetictracker.data.model.ExpiryStatus
import com.cosmetictracker.data.model.ProductStats
import com.cosmetictracker.data.model.UserProduct
import com.cosmetictracker.data.model.Category
import com.cosmetictracker.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.io.File

class ProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    init {
        loadProducts()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = productRepository.getCategories()
            if (result.isSuccess) {
                _categories.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            val result = productRepository.getUserProducts()
            _uiState.value = if (result.isSuccess) {
                val products = result.getOrNull() ?: emptyList()
                ProductsUiState.Success(products, calculateStats(products))
            } else {
                ProductsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load products")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteUserProduct(productId)
            loadProducts()
        }
    }
    
    fun addProduct(
        name: String,
        brandName: String,
        categoryId: String,
        barcode: String?,
        notes: String?,
        imageFile: File?,
        obfImageUrl: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var finalImageUrl = obfImageUrl
                if (imageFile != null) {
                    val uploadResult = productRepository.uploadImage(imageFile)
                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull()
                    }
                }

                val brandsResult = productRepository.getBrands()
                var brandId = ""
                if (brandsResult.isSuccess) {
                    val existing = brandsResult.getOrNull()?.find { it.name.equals(brandName, ignoreCase = true) }
                    brandId = existing?.id ?: ""
                }
                
                if (brandId.isEmpty()) {
                    val createBrandResult = productRepository.createBrand(brandName)
                    if (createBrandResult.isSuccess) {
                        brandId = createBrandResult.getOrNull()?.id ?: ""
                    } else {
                        onComplete(false, "Failed to create brand")
                        return@launch
                    }
                }

                val createProductResult = productRepository.createProduct(
                    name = name,
                    brandId = brandId,
                    categoryId = categoryId,
                    barcode = barcode?.takeIf { it.isNotBlank() },
                    description = notes,
                    paoMonths = 12
                )
                
                if (createProductResult.isFailure) {
                    onComplete(false, "Failed to create product in database")
                    return@launch
                }

                val productId = createProductResult.getOrNull()!!.id

                val createUserProductResult = productRepository.createUserProduct(
                    productId = productId,
                    purchasedAt = LocalDate.now().toString(),
                    openedAt = null,
                    notes = notes,
                    imageUrl = finalImageUrl
                )

                if (createUserProductResult.isSuccess) {
                    loadProducts()
                    onComplete(true, null)
                } else {
                    onComplete(false, "Failed to add product to your collection")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun getExpiryStatus(userProduct: UserProduct): ExpiryStatus {
        val openedAt = userProduct.openedAt ?: return ExpiryStatus.FRESH
        
        val openedDate = try {
            LocalDate.parse(openedAt, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            return ExpiryStatus.FRESH
        }

        val today = LocalDate.now()
        val monthsOpen = ChronoUnit.MONTHS.between(openedDate, today)
        val paoMonths = userProduct.product?.paoMonths?.toLong() ?: 12L

        return when {
            monthsOpen >= paoMonths -> ExpiryStatus.EXPIRED
            monthsOpen >= paoMonths - 1 -> ExpiryStatus.EXPIRING
            else -> ExpiryStatus.FRESH
        }
    }

    private fun calculateStats(products: List<UserProduct>): ProductStats {
        val total = products.size
        val active = products.count { it.openedAt != null && !it.isArchived }
        val expiringSoon = products.count { 
            getExpiryStatus(it) == ExpiryStatus.EXPIRING
        }
        return ProductStats(total, active, expiringSoon)
    }

    suspend fun getProductDetailsFromBarcode(barcode: String): com.cosmetictracker.data.remote.ObfProductData? {
        val result = productRepository.getProductByBarcode(barcode)
        return result.getOrNull()
    }
}

sealed class ProductsUiState {
    object Loading : ProductsUiState()
    data class Success(
        val products: List<UserProduct>,
        val stats: ProductStats
    ) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}
