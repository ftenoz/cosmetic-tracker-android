package com.cosmetictracker.data.repository

import com.cosmetictracker.data.model.*
import com.cosmetictracker.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

import com.cosmetictracker.data.remote.OpenBeautyFactsService
import com.cosmetictracker.data.remote.ObfProductData

class ProductRepository(
    private val api: ApiService,
    private val obfApi: OpenBeautyFactsService? = null
) {
    
    suspend fun getBrands(): Result<List<Brand>> {
        return try {
            val response = api.getBrands()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBrand(name: String): Result<Brand> {
        return try {
            val response = api.createBrand(CreateBrandRequest(name))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProducts(): Result<List<UserProduct>> {
        return try {
            val response = api.getUserProducts()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(
        name: String,
        brandId: String,
        categoryId: String,
        barcode: String?,
        description: String?,
        paoMonths: Int?
    ): Result<Product> {
        return try {
            val response = api.createProduct(
                CreateProductRequest(name, brandId, categoryId, barcode, description, paoMonths)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProduct(
        productId: String,
        purchasedAt: String?,
        openedAt: String?,
        notes: String?
    ): Result<UserProduct> {
        return try {
            val response = api.createUserProduct(
                CreateUserProductRequest(productId, purchasedAt, openedAt, notes)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProduct(
        id: String,
        purchasedAt: String?,
        openedAt: String?,
        notes: String?,
        imageUrl: String?
    ): Result<UserProduct> {
        return try {
            val response = api.updateUserProduct(
                id,
                UpdateUserProductRequest(purchasedAt, openedAt, notes, imageUrl)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserProduct(id: String): Result<Boolean> {
        return try {
            val response = api.deleteUserProduct(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(imageFile: File): Result<String> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                imageFile.name,
                requestBody
            )
            
            val response = api.uploadImage(multipartBody)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.url)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductByBarcode(barcode: String): Result<ObfProductData> {
        if (obfApi == null) return Result.failure(Exception("OBF API not initialized"))
        return try {
            val response = obfApi.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body() != null) {
                val obfResponse = response.body()!!
                if (obfResponse.status == 1 && obfResponse.product != null) {
                    Result.success(obfResponse.product)
                } else {
                    Result.failure(Exception("Product not found for barcode"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
