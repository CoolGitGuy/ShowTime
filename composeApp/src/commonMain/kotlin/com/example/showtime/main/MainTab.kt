package com.example.showtime.main

enum class MainTab(
    val route: String,
    val label: String,
    val badge: String,
    val subtitle: String
) {
    Movies(
        route = "movies",
        label = "Movies",
        badge = "MO",
        subtitle = "Paged catalog, filters and movie details live here."
    ),
    Favorites(
        route = "favorites",
        label = "Favorites",
        badge = "FA",
        subtitle = "Saved favorites synced from the local database."
    ),
    Watchlist(
        route = "watchlist",
        label = "Watchlist",
        badge = "WL",
        subtitle = "User watchlist and offline access start here."
    ),
    Quiz(
        route = "quiz",
        label = "Quiz",
        badge = "QZ",
        subtitle = "Timed movie quiz, score summary and leaderboard."
    ),
    Profile(
        route = "profile",
        label = "Profile",
        badge = "PR",
        subtitle = "Profile, logout and account settings belong here."
    );
}
