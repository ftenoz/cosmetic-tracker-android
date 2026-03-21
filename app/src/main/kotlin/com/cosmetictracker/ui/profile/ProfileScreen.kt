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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

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
                text = "Save Changes",
                onClick = { /* TODO */ }
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
