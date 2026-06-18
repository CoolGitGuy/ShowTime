package com.example.showtime.di

import com.example.showtime.database.AppDatabase
import com.example.showtime.app.AppShellViewModel
import com.example.showtime.auth.AuthApi
import com.example.showtime.auth.AuthRepository
import com.example.showtime.auth.AuthRepositoryImpl
import com.example.showtime.auth.AuthViewModel
import com.example.showtime.auth.SessionCoordinator
import com.example.showtime.auth.UserDataCleaner
import com.example.showtime.movies.data.LocalUserDataCleaner
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.movies.data.MovieRepositoryImpl
import com.example.showtime.movies.detail.MovieDetailsViewModel
import com.example.showtime.movies.list.MoviesCatalogViewModel
import com.example.showtime.networking.networkingModule
import com.example.showtime.profile.ProfileViewModel
import com.example.showtime.quiz.QuizViewModel
import com.example.showtime.session.PreferencesSessionStorage
import com.example.showtime.session.SessionStorage
import com.example.showtime.watchlist.WatchlistViewModel
import com.example.showtime.favorites.FavoritesViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private var koinInitialized = false

private val databaseModule = module {
    single { get<AppDatabase>().storageProbeDao() }
    single { get<AppDatabase>().movieDao() }
}

private val sessionModule = module {
    single<SessionStorage> { PreferencesSessionStorage(get()) }
}

private val authModule = module {
    single { SessionCoordinator(get(), getOrNull()) }
    single<AuthRepository> { AuthRepositoryImpl(get<AuthApi>(), get()) }
}

private val moviesModule = module {
    single<UserDataCleaner> { LocalUserDataCleaner(get()) }
    single<MovieRepository> { MovieRepositoryImpl(get(), get(), get()) }
}

private val appModule = module {
    viewModelOf(::AppShellViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MoviesCatalogViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::QuizViewModel)
    viewModel { params ->
        MovieDetailsViewModel(
            movieId = params.get(),
            movieRepository = get(),
            sessionStorage = get()
        )
    }
}

fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(
            listOf(
                databaseModule,
                sessionModule,
                networkingModule,
                authModule,
                moviesModule,
                appModule
            ) + platformStorageModules()
        )
    }
}

fun initKoinIfNeeded(
    appDeclaration: KoinAppDeclaration = {}
) {
    if (koinInitialized) {
        return
    }

    initKoin(appDeclaration)
    koinInitialized = true
}
