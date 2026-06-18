package com.example.showtime.di

import com.example.showtime.database.AppDatabase
import com.example.showtime.app.AppShellViewModel
import com.example.showtime.auth.AuthApi
import com.example.showtime.auth.AuthRepository
import com.example.showtime.auth.AuthRepositoryImpl
import com.example.showtime.auth.AuthViewModel
import com.example.showtime.auth.SessionCoordinator
import com.example.showtime.networking.networkingModule
import com.example.showtime.profile.ProfileViewModel
import com.example.showtime.session.PreferencesSessionStorage
import com.example.showtime.session.SessionStorage
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private var koinInitialized = false

private val databaseModule = module {
    single { get<AppDatabase>().storageProbeDao() }
}

private val sessionModule = module {
    single<SessionStorage> { PreferencesSessionStorage(get()) }
}

private val authModule = module {
    single { SessionCoordinator(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get<AuthApi>(), get()) }
}

private val appModule = module {
    viewModelOf(::AppShellViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
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
