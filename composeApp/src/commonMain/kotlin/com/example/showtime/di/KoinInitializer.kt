package com.example.showtime.di

import com.example.showtime.database.AppDatabase
import com.example.showtime.app.AppShellViewModel
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

private val appModule = module {
    viewModelOf(::AppShellViewModel)
}

fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(listOf(databaseModule, sessionModule, appModule) + platformStorageModules())
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
