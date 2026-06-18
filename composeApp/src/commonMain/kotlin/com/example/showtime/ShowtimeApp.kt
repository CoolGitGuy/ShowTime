package com.example.showtime

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.showtime.app.AppShellScreen
import com.example.showtime.app.AppShellViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShowtimeApp() {
    val viewModel = koinViewModel<AppShellViewModel>()

    MaterialTheme {
        AppShellScreen(viewModel = viewModel)
    }
}
