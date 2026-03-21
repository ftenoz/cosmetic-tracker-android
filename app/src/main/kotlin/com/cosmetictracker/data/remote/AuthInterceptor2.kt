package com.cosmetictracker.data.remote

import android.util.Log
import com.cosmetictracker.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor2(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getToken().firstOrNull()
        }

        Log.d("AuthInterceptor", "Token: ${if (token != null) "Present (${token.take(20)}...)" else "NULL"}")

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        Log.d("AuthInterceptor", "Request URL: ${request.url}")
        Log.d("AuthInterceptor", "Has Auth Header: ${request.header("Authorization") != null}")

        return chain.proceed(request)
    }
}
