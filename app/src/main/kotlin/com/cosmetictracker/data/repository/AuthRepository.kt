package com.cosmetictracker.data.repository

import com.cosmetictracker.data.local.TokenManager
import com.cosmetictracker.data.model.LoginRequest
import com.cosmetictracker.data.model.RegisterRequest
import com.cosmetictracker.data.model.User
import com.cosmetictracker.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

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
            val response = api.login(LoginRequest(email, password))
            
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
