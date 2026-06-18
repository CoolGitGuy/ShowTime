package com.example.showtime.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    viewModel: AuthViewModel
) {
    val state by viewModel.state.collectAsState()

    AuthScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun AuthScreen(
    state: AuthContract.State,
    onIntent: (AuthContract.Intent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Showtime",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Sign in or create an account to unlock catalog sync, favorites, watchlist and quiz history.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TabRow(selectedTabIndex = state.mode.ordinal) {
                AuthContract.Mode.entries.forEach { mode ->
                    Tab(
                        selected = state.mode == mode,
                        onClick = { onIntent(AuthContract.Intent.ModeChanged(mode)) },
                        text = {
                            Text(
                                text = if (mode == AuthContract.Mode.Login) {
                                    "Login"
                                } else {
                                    "Sign up"
                                }
                            )
                        }
                    )
                }
            }

            if (state.mode == AuthContract.Mode.SignUp) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.fullName,
                    onValueChange = { onIntent(AuthContract.Intent.FullNameChanged(it)) },
                    label = { Text("Full name") },
                    singleLine = true,
                    enabled = !state.isLoading
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.username,
                onValueChange = { onIntent(AuthContract.Intent.UsernameChanged(it)) },
                label = { Text("Username") },
                singleLine = true,
                enabled = !state.isLoading
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.password,
                onValueChange = { onIntent(AuthContract.Intent.PasswordChanged(it)) },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !state.isLoading
            )

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { onIntent(AuthContract.Intent.Submit) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Text(
                    text = if (state.mode == AuthContract.Mode.Login) {
                        "Continue to Showtime"
                    } else {
                        "Create account"
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
        }
    }
}
