package com.cosmetictracker.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmetictracker.data.model.ExpiryStatus
import com.cosmetictracker.data.model.ProductStats
import com.cosmetictracker.data.model.UserProduct
import com.cosmetictracker.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState

    init {
        loadProducts()
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
}

sealed class ProductsUiState {
    object Loading : ProductsUiState()
    data class Success(
        val products: List<UserProduct>,
        val stats: ProductStats
    ) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}
