package com.cosmetictracker.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String?,
    val createdAt: String,
    val updatedAt: String
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)

// Brand
data class Brand(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)

data class CreateBrandRequest(
    val name: String
)

// Category
data class Category(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)

// Product
data class Product(
    val id: String,
    val name: String,
    val brandId: String,
    val categoryId: String,
    val barcode: String?,
    val description: String?,
    val paoMonths: Int?,
    val brand: Brand?,
    val category: Category?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateProductRequest(
    val name: String,
    val brandId: String,
    val categoryId: String,
    val barcode: String? = null,
    val description: String? = null,
    val paoMonths: Int? = null
)

// User Product
data class UserProduct(
    val id: String,
    val userId: String,
    val productId: String,
    val purchasedAt: String?,
    val openedAt: String?,
    val notes: String?,
    val imageUrl: String?,
    val isArchived: Boolean,
    val product: Product?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateUserProductRequest(
    val productId: String,
    val purchasedAt: String? = null,
    val openedAt: String? = null,
    val notes: String? = null
)

data class UpdateUserProductRequest(
    val purchasedAt: String? = null,
    val openedAt: String? = null,
    val notes: String? = null,
    val imageUrl: String? = null,
    val isArchived: Boolean? = null
)

// Upload
data class UploadResponse(
    val url: String
)

// Delete
data class DeleteResponse(
    val message: String
)

// Barcode API (Open Beauty Facts)
data class BarcodeProduct(
    val found: Boolean,
    val productName: String? = null,
    val brandName: String? = null,
    val imageUrl: String? = null,
    val categories: List<String>? = null
)

// UI Models
data class ProductStats(
    val total: Int = 0,
    val active: Int = 0,
    val expiringSoon: Int = 0
)

enum class ExpiryStatus {
    FRESH, EXPIRING, EXPIRED
}
