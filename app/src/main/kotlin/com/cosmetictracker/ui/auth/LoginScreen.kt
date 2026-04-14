package com.cosmetictracker.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTSecondaryButton
import com.cosmetictracker.ui.components.CTTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            viewModel.resetState() // Clear state BEFORE navigating to prevent re-trigger on recompose
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email
            CTTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "your@email.com",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            // Password
            CTTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            // Error message
            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login button
            CTButton(
                text = "Login",
                onClick = { viewModel.login(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                isLoading = uiState is LoginUiState.Loading
            )

            // Register button
            CTSecondaryButton(
                text = "Create Account",
                onClick = onNavigateToRegister
            )
        }
    }
}
