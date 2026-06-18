package com.example.showtime.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppShellScreen(
    viewModel: AppShellViewModel
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.destination) {
        val route = state.destination.route
        if (navController.currentDestination?.route == route) {
            return@LaunchedEffect
        }

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destination.Splash.route
    ) {
        composable(Destination.Splash.route) {
            ShellPlaceholderScreen(
                title = "Loading session",
                subtitle = "Preparing Showtime..."
            ) {
                CircularProgressIndicator()
            }
        }

        composable(Destination.Auth.route) {
            ShellPlaceholderScreen(
                title = "Auth Landing",
                subtitle = "Login and signup flow will live here."
            )
        }

        composable(Destination.Main.route) {
            ShellPlaceholderScreen(
                title = "Main App Shell",
                subtitle = "Movies, quiz, favorites and profile start here."
            )
        }
    }
}

private val Destination.route: String
    get() = when (this) {
        Destination.Splash -> "splash"
        Destination.Auth -> "auth"
        Destination.Main -> "main"
    }

@Composable
private fun ShellPlaceholderScreen(
    title: String,
    subtitle: String,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content?.invoke()
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
