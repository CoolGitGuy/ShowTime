package com.example.showtime.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.showtime.profile.ProfileScreen
import com.example.showtime.profile.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(MainTab.Movies) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                text = tab.badge,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        label = {
                            Text(text = tab.label)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (selectedTab == MainTab.Profile) {
            val profileViewModel = koinViewModel<ProfileViewModel>()
            ProfileScreen(viewModel = profileViewModel)
        } else {
            MainTabPlaceholder(
                tab = selectedTab,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun MainTabPlaceholder(
    tab: MainTab,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tab.label,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = tab.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
