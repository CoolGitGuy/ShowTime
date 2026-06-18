package com.example.showtime

import androidx.compose.ui.window.ComposeUIViewController
import com.example.showtime.di.initKoinIfNeeded

fun MainViewController() = ComposeUIViewController {
    initKoinIfNeeded()
    ShowtimeApp()
}
