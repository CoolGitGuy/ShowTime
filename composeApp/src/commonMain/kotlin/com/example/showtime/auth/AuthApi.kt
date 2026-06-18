package com.example.showtime.auth

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): AuthResponse

    @GET("me")
    suspend fun getMe(): UserDto
}
