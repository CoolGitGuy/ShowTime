package com.example.showtime.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class SignUpRequest(
    @SerialName("full_name")
    val fullName: String,
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    @SerialName("full_name")
    val fullName: String
)

data class UserProfile(
    val id: Int,
    val username: String,
    val fullName: String
)
