package com.cosmetictracker.data.remote

import com.cosmetictracker.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Users
    @GET("users/me")
    suspend fun getProfile(): Response<User>

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateUserRequest): Response<User>

    // Brands
    @GET("brands")
    suspend fun getBrands(): Response<List<Brand>>

    @POST("brands")
    suspend fun createBrand(@Body request: CreateBrandRequest): Response<Brand>

    // Categories
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    // Products
    @GET("products")
    suspend fun getProducts(@Query("q") query: String? = null): Response<List<Product>>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<Product>

    // User Products
    @GET("user-products")
    suspend fun getUserProducts(): Response<List<UserProduct>>

    @POST("user-products")
    suspend fun createUserProduct(@Body request: CreateUserProductRequest): Response<UserProduct>

    @PATCH("user-products/{id}")
    suspend fun updateUserProduct(
        @Path("id") id: String,
        @Body request: UpdateUserProductRequest
    ): Response<UserProduct>

    @DELETE("user-products/{id}")
    suspend fun deleteUserProduct(@Path("id") id: String): Response<DeleteResponse>

    // Upload
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): Response<UploadResponse>
}
