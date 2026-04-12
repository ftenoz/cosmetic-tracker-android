package com.cosmetictracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmetictracker.ui.components.CTButton
import com.cosmetictracker.ui.components.CTTextField

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var firstName by remember(uiState.firstName) { mutableStateOf(uiState.firstName) }
    var lastName by remember(uiState.lastName) { mutableStateOf(uiState.lastName) }
    var email by remember(uiState.email) { mutableStateOf(uiState.email) }

    LaunchedEffect(uiState.isSuccess, uiState.error) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.onStateHandled()
        }
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
            viewModel.onStateHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile Settings",
                style = MaterialTheme.typography.headlineSmall
            )

            CTTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            CTTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name"
            )

            CTTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name"
            )

            Spacer(modifier = Modifier.weight(1f))

            CTButton(
                text = if (uiState.isLoading) "Saving..." else "Save Changes",
                onClick = { viewModel.updateProfile(firstName, lastName, email) },
                enabled = !uiState.isLoading
            )

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Logout",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
