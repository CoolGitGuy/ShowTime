package com.example.showtime.networking

import com.example.showtime.auth.AuthApi
import com.example.showtime.auth.SessionCoordinator
import com.example.showtime.auth.createAuthApi
import com.example.showtime.movies.data.MovieApi
import com.example.showtime.movies.data.ShowtimeUserApi
import com.example.showtime.movies.data.createMovieApi
import com.example.showtime.movies.data.createShowtimeUserApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val BASE_URL = "https://rma.finlab.rs/"

private val publicPaths = setOf("/auth/login", "/auth/signup")

val networkingModule = module {
    single<HttpClient> {
        val sessionCoordinator = get<SessionCoordinator>()
        val sessionStorage = get<com.example.showtime.session.SessionStorage>()

        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStorage.session.value?.accessToken?.let { token ->
                            BearerTokens(token, "")
                        }
                    }
                    sendWithoutRequest { request ->
                        requestPath(request.url.toString()) !in publicPaths
                    }
                }
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    val isUnauthorized = cause is io.ktor.client.plugins.ClientRequestException &&
                        cause.response.status == HttpStatusCode.Unauthorized
                    val path = requestPath(request.url.toString())
                    if (isUnauthorized && path !in publicPaths) {
                        sessionCoordinator.logout()
                    }
                    throw cause
                }
            }
        }
    }

    single<Ktorfit> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .baseUrl(url = BASE_URL)
            .build()
    }

    single<AuthApi> {
        get<Ktorfit>().createAuthApi()
    }

    single<MovieApi> {
        get<Ktorfit>().createMovieApi()
    }

    single<ShowtimeUserApi> {
        get<Ktorfit>().createShowtimeUserApi()
    }
}

private fun requestPath(rawUrl: String): String {
    val withoutHost = rawUrl.substringAfter("https://rma.finlab.rs", rawUrl)
    return withoutHost.substringBefore("?")
}
