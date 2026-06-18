package com.example.showtime.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileContract.State,
    onIntent: (ProfileContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        state.session?.let { session ->
            Text(
                text = session.fullName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "@${session.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "User ID: ${session.userId}",
                style = MaterialTheme.typography.bodyMedium
            )
        } ?: Text(
            text = "No active user session.",
            style = MaterialTheme.typography.bodyLarge
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedButton(
            onClick = { onIntent(ProfileContract.Intent.Refresh) },
            enabled = !state.isRefreshing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh profile")
        }

        Button(
            onClick = { onIntent(ProfileContract.Intent.Logout) },
            enabled = !state.isRefreshing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            }
            Text("Logout")
        }
    }
}
