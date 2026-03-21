package com.cosmetictracker

import android.app.Application
import com.cosmetictracker.data.local.TokenManager
import com.cosmetictracker.data.remote.RetrofitClient
import com.cosmetictracker.data.repository.AuthRepository
import com.cosmetictracker.data.repository.ProductRepository

class CosmeticTrackerApplication : Application() {
    
    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var productRepository: ProductRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize dependencies
        tokenManager = TokenManager(applicationContext)
        
        val apiService = RetrofitClient.create(tokenManager)
        
        authRepository = AuthRepository(apiService, tokenManager)
        productRepository = ProductRepository(apiService)
    }
}
