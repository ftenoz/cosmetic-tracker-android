package com.cosmetictracker.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class ObfResponse(
    val status: Int,
    val status_verbose: String?,
    val code: String?,
    val product: ObfProductData?
)

data class ObfProductData(
    val product_name: String?,
    val brands: String?,
    val image_front_url: String?,
    val categories: String?
)

interface OpenBeautyFactsService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<ObfResponse>
}
