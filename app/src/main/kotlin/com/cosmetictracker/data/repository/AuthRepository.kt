package com.cosmetictracker.data.repository

import com.cosmetictracker.data.local.TokenManager
import com.cosmetictracker.data.model.LoginRequest
import com.cosmetictracker.data.model.RegisterRequest
import com.cosmetictracker.data.model.User
import com.cosmetictracker.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String?
    ): Result<User> {
        return try {
            val response = api.register(
                RegisterRequest(email, password, firstName, lastName)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveToken(authResponse.accessToken)
                tokenManager.saveUserInfo(
                    authResponse.user.id,
                    authResponse.user.email,
                    authResponse.user.firstName,
                    authResponse.user.lastName
                )
                Result.success(authResponse.user)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            android.util.Log.d("AuthRepository", "Login attempt for: $email")
            val response = api.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                android.util.Log.d("AuthRepository", "Login successful, saving token...")
                tokenManager.saveToken(authResponse.accessToken)
                tokenManager.saveUserInfo(
                    authResponse.user.id,
                    authResponse.user.email,
                    authResponse.user.firstName,
                    authResponse.user.lastName
                )
                android.util.Log.d("AuthRepository", "Token saved: ${authResponse.accessToken.take(20)}...")
                
                // Verify token was saved
                kotlinx.coroutines.delay(100) // Give DataStore time to write
                val savedToken = tokenManager.getToken().firstOrNull()
                android.util.Log.d("AuthRepository", "Token verification: ${savedToken?.take(20) ?: "NULL"}")
                
                Result.success(authResponse.user)
            } else {
                android.util.Log.e("AuthRepository", "Login failed: ${response.code()} ${response.message()}")
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Login exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearAll()
    }

    fun getToken(): Flow<String?> = tokenManager.getToken()
    
    fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.getToken().let { flow ->
            kotlinx.coroutines.flow.map(flow) { it != null }
        }
    }
}
