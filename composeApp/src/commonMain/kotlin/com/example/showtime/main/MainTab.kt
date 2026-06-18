package com.example.showtime.main

enum class MainTab(
    val label: String,
    val badge: String,
    val subtitle: String
) {
    Movies(
        label = "Movies",
        badge = "MO",
        subtitle = "Paged catalog, filters and movie details live here."
    ),
    Favorites(
        label = "Favorites",
        badge = "FA",
        subtitle = "Saved favorites synced from the local database."
    ),
    Watchlist(
        label = "Watchlist",
        badge = "WL",
        subtitle = "User watchlist and offline access start here."
    ),
    Quiz(
        label = "Quiz",
        badge = "QZ",
        subtitle = "Movie quiz flow and scoring will live here."
    ),
    Profile(
        label = "Profile",
        badge = "PR",
        subtitle = "Profile, logout and account settings belong here."
    );
}
