package com.example.showtime.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val error: String? = null,
    @SerialName("httpCode")
    val httpCode: Int? = null,
    val message: String? = null,
    val description: String? = null,
    val suggestion: String? = null
)
