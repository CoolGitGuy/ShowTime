package com.example.showtime.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.showtime.favorites.FavoritesScreen
import com.example.showtime.favorites.FavoritesViewModel
import com.example.showtime.movies.detail.MovieDetailsScreen
import com.example.showtime.movies.detail.MovieDetailsViewModel
import com.example.showtime.movies.list.MoviesCatalogScreen
import com.example.showtime.movies.list.MoviesCatalogViewModel
import com.example.showtime.profile.ProfileScreen
import com.example.showtime.profile.ProfileViewModel
import com.example.showtime.quiz.QuizScreen
import com.example.showtime.quiz.QuizViewModel
import com.example.showtime.watchlist.WatchlistScreen
import com.example.showtime.watchlist.WatchlistViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedMovieId by remember { mutableStateOf<String?>(null) }
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = MainTab.entries.firstOrNull { it.route == currentRoute } ?: MainTab.Movies
    val isTopLevelDestination = MainTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Movies.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainTab.Movies.route) {
                val viewModel = koinViewModel<MoviesCatalogViewModel>()
                MoviesCatalogScreen(
                    viewModel = viewModel,
                    onMovieClick = { movieId ->
                        selectedMovieId = movieId
                        navController.navigate("movie")
                    }
                )
            }

            composable(MainTab.Favorites.route) {
                val viewModel = koinViewModel<FavoritesViewModel>()
                FavoritesScreen(
                    viewModel = viewModel,
                    onMovieClick = { movieId ->
                        selectedMovieId = movieId
                        navController.navigate("movie")
                    }
                )
            }

            composable(MainTab.Watchlist.route) {
                val viewModel = koinViewModel<WatchlistViewModel>()
                WatchlistScreen(
                    viewModel = viewModel,
                    onMovieClick = { movieId ->
                        selectedMovieId = movieId
                        navController.navigate("movie")
                    }
                )
            }

            composable(MainTab.Quiz.route) {
                val quizViewModel = koinViewModel<QuizViewModel>()
                QuizScreen(viewModel = quizViewModel)
            }

            composable(MainTab.Profile.route) {
                val profileViewModel = koinViewModel<ProfileViewModel>()
                ProfileScreen(viewModel = profileViewModel)
            }

            composable("movie") {
                val movieId = selectedMovieId.orEmpty()
                val viewModel = koinViewModel<MovieDetailsViewModel>(
                    parameters = { parametersOf(movieId) }
                )
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Back")
                    }
                    MovieDetailsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
